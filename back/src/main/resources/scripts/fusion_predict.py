import json
import math
import os
import random
import sys
from dataclasses import dataclass


def _safe_float(x, default=0.0):
    try:
        if x is None:
            return default
        if isinstance(x, (int, float)):
            return float(x)
        s = str(x).strip()
        if s == "":
            return default
        return float(s)
    except Exception:
        return default


def _teacher_predict(vision, iot):
    maturity = str(vision.get("maturity", "未知"))
    category = str(vision.get("category", "水果"))

    temp_avg = _safe_float(iot.get("temperature_avg"), 0.0)
    temp_fluc = _safe_float(iot.get("temperature_fluctuation"), 0.0)
    humidity = _safe_float(iot.get("humidity"), 0.0)
    ethylene = _safe_float(iot.get("ethylene"), 0.0)

    is_rotten = "腐烂" in maturity

    if is_rotten:
        shelf_life = 0.0
        risk = 2
        fusion_text = (
            "【多模态融合分析】\n"
            f"综合当前视觉状态（{maturity}）与过去24小时冷链IoT数据（平均温度{temp_avg:.1f}℃，波动{temp_fluc:.1f}℃）：\n"
            f"该批次已变质，且乙烯浓度偏高（{ethylene:.2f}ppm），建议立即隔离，避免加速同车厢其他果蔬腐烂。"
        )
        return {
            "shelf_life_days": shelf_life,
            "risk_level": "high",
            "fusion_prediction": fusion_text,
            "debug": {"humidity": humidity},
        }

    shelf_life = 7.0
    if temp_avg > 5.0:
        shelf_life -= (temp_avg - 5.0) * 0.5
    if temp_fluc > 2.0:
        shelf_life -= 1.0
    if ethylene > 1.0:
        shelf_life -= (ethylene - 1.0) * 1.5
    if shelf_life < 0.5:
        shelf_life = 0.5

    if shelf_life < 1.5:
        risk = 2
        risk_level = "high"
    elif shelf_life < 3.0:
        risk = 1
        risk_level = "medium"
    else:
        risk = 0
        risk_level = "low"

    fusion_text = (
        "【多模态融合分析】\n"
        f"综合当前视觉状态（{maturity}）与过去24小时冷链IoT数据（平均温度{temp_avg:.1f}℃，波动{temp_fluc:.1f}℃）：\n"
        f"预计该批次「{category}」剩余保质期还有 {shelf_life:.1f} 天。"
    )
    if temp_fluc > 2.0:
        fusion_text += " ⚠️警告：近期温度波动较大，建议检查冷库设备。"

    return {
        "shelf_life_days": shelf_life,
        "risk_level": risk_level,
        "risk_class": risk,
        "fusion_prediction": fusion_text,
        "debug": {"humidity": humidity},
    }


@dataclass
class FusionConfig:
    d_model: int = 64
    num_queries: int = 8
    num_heads: int = 4
    num_layers: int = 2
    hidden: int = 128


def _normalize_iot(temp_avg, temp_fluc, humidity, ethylene):
    x0 = (temp_avg - 5.5) / 2.0
    x1 = (temp_fluc - 2.5) / 2.0
    x2 = (humidity - 90.0) / 10.0
    x3 = (ethylene - 1.25) / 1.5
    return [x0, x1, x2, x3]


def _vision_to_ids(vision):
    maturity_level = str(vision.get("maturity_level", "unknown"))
    maturity = str(vision.get("maturity", "未知"))
    if "rotten" in maturity_level or "腐烂" in maturity:
        m_id = 1
    elif "fresh" in maturity_level or "新鲜" in maturity:
        m_id = 0
    else:
        m_id = 2

    category = str(vision.get("category", "水果"))
    cat_vocab = ["苹果", "橙子", "香蕉", "芒果", "葡萄", "草莓", "石榴", "水果", "未识别"]
    if category in cat_vocab:
        c_id = cat_vocab.index(category)
    else:
        c_id = len(cat_vocab) - 2
    return m_id, c_id


def _load_torch():
    try:
        import torch
        import torch.nn as nn
        return torch, nn
    except Exception:
        return None, None


def _build_model(torch, nn, cfg: FusionConfig):
    class QFormerFusion(nn.Module):
        def __init__(self):
            super().__init__()
            self.cfg = cfg
            self.maturity_emb = nn.Embedding(3, cfg.d_model)
            self.category_emb = nn.Embedding(9, cfg.d_model)
            self.vision_scalar = nn.Sequential(
                nn.Linear(1, cfg.d_model),
                nn.GELU(),
            )
            self.iot_scalar = nn.Sequential(
                nn.Linear(1, cfg.d_model),
                nn.GELU(),
            )
            self.query = nn.Parameter(torch.randn(cfg.num_queries, cfg.d_model) * 0.02)
            layers = []
            for _ in range(cfg.num_layers):
                layers.append(nn.MultiheadAttention(cfg.d_model, cfg.num_heads, batch_first=True))
            self.attn_layers = nn.ModuleList(layers)
            self.ffn = nn.Sequential(
                nn.Linear(cfg.d_model, cfg.hidden),
                nn.GELU(),
                nn.Linear(cfg.hidden, cfg.d_model),
            )
            self.norm = nn.LayerNorm(cfg.d_model)
            self.head_reg = nn.Sequential(
                nn.Linear(cfg.d_model, cfg.hidden),
                nn.GELU(),
                nn.Linear(cfg.hidden, 1),
            )
            self.head_cls = nn.Sequential(
                nn.Linear(cfg.d_model, cfg.hidden),
                nn.GELU(),
                nn.Linear(cfg.hidden, 3),
            )

        def forward(self, maturity_id, category_id, conf, iot4):
            bsz = maturity_id.shape[0]
            v0 = self.maturity_emb(maturity_id)
            v1 = self.category_emb(category_id)
            v2 = self.vision_scalar(conf)
            vision_token = (v0 + v1 + v2).unsqueeze(1)

            iot_tokens = self.iot_scalar(iot4.unsqueeze(-1))
            tokens = torch.cat([vision_token, iot_tokens], dim=1)

            q = self.query.unsqueeze(0).expand(bsz, -1, -1)
            for attn in self.attn_layers:
                delta, _ = attn(q, tokens, tokens, need_weights=False)
                q = self.norm(q + delta)
                q = self.norm(q + self.ffn(q))
            pooled = q.mean(dim=1)
            shelf = self.head_reg(pooled).squeeze(-1)
            risk_logits = self.head_cls(pooled)
            return shelf, risk_logits

    return QFormerFusion()


def _default_model_path():
    base = os.path.join(os.getcwd(), "models")
    os.makedirs(base, exist_ok=True)
    return os.path.join(base, "fusion_qformer_distilled.pt")


def _train_distill_if_needed(model_path, torch, nn, cfg: FusionConfig):
    if os.path.exists(model_path):
        return

    lock_path = model_path + ".lock"
    try:
        fd = os.open(lock_path, os.O_CREAT | os.O_EXCL | os.O_WRONLY)
        os.close(fd)
        have_lock = True
    except Exception:
        have_lock = False

    if not have_lock:
        return

    try:
        model = _build_model(torch, nn, cfg)
        model.train()
        optim = torch.optim.AdamW(model.parameters(), lr=2e-3, weight_decay=1e-2)
        loss_mse = nn.SmoothL1Loss()
        loss_ce = nn.CrossEntropyLoss()

        steps = int(os.environ.get("FUSION_DISTILL_STEPS", "300"))
        batch = int(os.environ.get("FUSION_DISTILL_BATCH", "128"))

        for _ in range(steps):
            maturity_ids = []
            category_ids = []
            confs = []
            iot_rows = []
            y_shelf = []
            y_risk = []

            for _i in range(batch):
                r = random.random()
                if r < 0.15:
                    maturity = "腐烂"
                    maturity_level = "rotten"
                elif r < 0.95:
                    maturity = "新鲜"
                    maturity_level = "fresh"
                else:
                    maturity = "成熟度未知"
                    maturity_level = "unknown"

                cat_vocab = ["苹果", "橙子", "香蕉", "芒果", "葡萄", "草莓", "石榴", "水果", "未识别"]
                category = random.choice(cat_vocab)
                conf = max(0.0, min(1.0, random.random() ** 0.5))

                temp_avg = 4.5 + random.random() * 2.0
                temp_fluc = 1.0 + random.random() * 3.0
                humidity = 85.0 + random.random() * 10.0
                ethylene = 0.5 + random.random() * 1.5

                vision = {"maturity": maturity, "maturity_level": maturity_level, "category": category}
                iot = {
                    "temperature_avg": temp_avg,
                    "temperature_fluctuation": temp_fluc,
                    "humidity": humidity,
                    "ethylene": ethylene,
                }
                teacher = _teacher_predict(vision, iot)
                shelf = float(teacher.get("shelf_life_days", 0.0))
                risk_level = str(teacher.get("risk_level", "medium"))
                risk = 1
                if risk_level == "low":
                    risk = 0
                elif risk_level == "high":
                    risk = 2

                m_id, c_id = _vision_to_ids(vision)
                maturity_ids.append(m_id)
                category_ids.append(c_id)
                confs.append([conf])
                iot_rows.append(_normalize_iot(temp_avg, temp_fluc, humidity, ethylene))
                y_shelf.append(shelf)
                y_risk.append(risk)

            maturity_ids = torch.tensor(maturity_ids, dtype=torch.long)
            category_ids = torch.tensor(category_ids, dtype=torch.long)
            confs = torch.tensor(confs, dtype=torch.float32)
            iot_rows = torch.tensor(iot_rows, dtype=torch.float32)
            y_shelf = torch.tensor(y_shelf, dtype=torch.float32)
            y_risk = torch.tensor(y_risk, dtype=torch.long)

            pred_shelf, pred_risk = model(maturity_ids, category_ids, confs, iot_rows)
            loss = loss_mse(pred_shelf, y_shelf) + 0.5 * loss_ce(pred_risk, y_risk)

            optim.zero_grad(set_to_none=True)
            loss.backward()
            torch.nn.utils.clip_grad_norm_(model.parameters(), 1.0)
            optim.step()

        tmp = model_path + ".tmp"
        torch.save(
            {
                "state_dict": model.state_dict(),
                "cfg": cfg.__dict__,
            },
            tmp,
        )
        os.replace(tmp, model_path)
    finally:
        try:
            os.remove(lock_path)
        except Exception:
            pass


def _predict_with_model(payload, model_path, torch, nn):
    cfg = FusionConfig()
    _train_distill_if_needed(model_path, torch, nn, cfg)

    if not os.path.exists(model_path):
        return None

    ckpt = torch.load(model_path, map_location="cpu")
    saved_cfg = ckpt.get("cfg") or {}
    cfg = FusionConfig(**{k: saved_cfg.get(k, getattr(FusionConfig(), k)) for k in FusionConfig().__dict__.keys() if not k.startswith("_")})
    model = _build_model(torch, nn, cfg)
    model.load_state_dict(ckpt["state_dict"], strict=True)
    model.eval()

    vision = payload.get("vision") or {}
    iot = payload.get("iot") or {}

    m_id, c_id = _vision_to_ids(vision)
    conf = _safe_float(vision.get("confidence"), 0.0)
    temp_avg = _safe_float(iot.get("temperature_avg"), 0.0)
    temp_fluc = _safe_float(iot.get("temperature_fluctuation"), 0.0)
    humidity = _safe_float(iot.get("humidity"), 0.0)
    ethylene = _safe_float(iot.get("ethylene"), 0.0)
    iot4 = _normalize_iot(temp_avg, temp_fluc, humidity, ethylene)

    with torch.no_grad():
        pred_shelf, pred_risk = model(
            torch.tensor([m_id], dtype=torch.long),
            torch.tensor([c_id], dtype=torch.long),
            torch.tensor([[conf]], dtype=torch.float32),
            torch.tensor([iot4], dtype=torch.float32),
        )
        shelf = float(pred_shelf.item())
        shelf = max(0.0, min(10.0, shelf))
        logits = pred_risk[0].tolist()
        exps = [math.exp(x - max(logits)) for x in logits]
        s = sum(exps)
        probs = [e / s for e in exps]
        cls = int(max(range(3), key=lambda i: probs[i]))

    risk_level = "low" if cls == 0 else ("medium" if cls == 1 else "high")

    maturity = str(vision.get("maturity", "未知"))
    category = str(vision.get("category", "水果"))
    fusion_text = (
        "【多模态融合分析】\n"
        f"综合当前视觉状态（{maturity}）与过去24小时冷链IoT数据（平均温度{temp_avg:.1f}℃，波动{temp_fluc:.1f}℃）：\n"
        f"预计该批次「{category}」剩余保质期还有 {shelf:.1f} 天。"
    )
    if temp_fluc > 2.0:
        fusion_text += " ⚠️警告：近期温度波动较大，建议检查冷库设备。"
    if risk_level == "high":
        fusion_text += " 风险较高，建议优先出库或二次质检。"

    return {
        "shelf_life_days": round(shelf, 2),
        "risk_level": risk_level,
        "risk_prob": {"low": round(probs[0], 4), "medium": round(probs[1], 4), "high": round(probs[2], 4)},
        "fusion_prediction": fusion_text,
    }


def main():
    raw = sys.stdin.buffer.read().decode("utf-8", errors="replace")
    if raw is None or raw.strip() == "":
        print(json.dumps({"error": "empty_input"}, ensure_ascii=False))
        return

    try:
        payload = json.loads(raw)
    except Exception as e:
        print(json.dumps({"error": "invalid_json", "details": str(e)}, ensure_ascii=False))
        return

    torch, nn = _load_torch()
    model_path = os.environ.get("FUSION_MODEL_PATH") or _default_model_path()

    if torch is not None:
        pred = _predict_with_model(payload, model_path, torch, nn)
        if pred is not None:
            print(json.dumps(pred, ensure_ascii=False))
            return

    vision = payload.get("vision") or {}
    iot = payload.get("iot") or {}
    print(json.dumps(_teacher_predict(vision, iot), ensure_ascii=False))


if __name__ == "__main__":
    main()

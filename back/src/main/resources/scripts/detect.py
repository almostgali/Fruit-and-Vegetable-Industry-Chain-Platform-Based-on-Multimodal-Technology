import argparse
import json
import os
import sys
from PIL import Image

# 禁用YOLO的详细输出
os.environ['YOLO_VERBOSE'] = 'False'

def parse_args():
    parser = argparse.ArgumentParser(description='Fruit Quality and Maturity Detection')
    parser.add_argument('--source', type=str, required=True, help='source image path')
    parser.add_argument('--weights', type=str, required=True, help='model weights path')
    parser.add_argument('--type', type=str, default='quality', help='detection type: quality or maturity')
    return parser.parse_args()

def load_model(weights_path, model_type):
    """加载YOLO模型"""
    try:
        # 检查模型文件是否存在
        if not os.path.exists(weights_path):
            return None
        
        # 导入ultralytics YOLO
        from ultralytics import YOLO
        import warnings
        warnings.filterwarnings("ignore")
        
        # 重定向stdout到devnull来禁用YOLO输出
        import contextlib
        with open(os.devnull, 'w') as devnull:
            with contextlib.redirect_stdout(devnull):
                model = YOLO(weights_path)
        
        return model
        
    except ImportError as e:
        return None
    except Exception as e:
        return None

def preprocess_image(image_path):
    # 简化图像预处理
    try:
        # 处理路径编码问题，确保能正确读取包含中文字符的路径
        import os
        import pathlib
        
        # 使用pathlib处理路径编码问题
        path_obj = pathlib.Path(image_path)
        
        if path_obj.exists():
            # 读取文件内容到内存，然后用BytesIO处理
            import io
            file_content = path_obj.read_bytes()
            image = Image.open(io.BytesIO(file_content)).convert('RGB')
            return image
        else:
            print(f"Error: Image file not found: {image_path}", file=sys.stderr)
            print(f"Path exists check: {path_obj.exists()}", file=sys.stderr)
            sys.exit(1)
    except Exception as e:
        print(f"Error preprocessing image: {e}", file=sys.stderr)
        print(f"Image path: {repr(image_path)}", file=sys.stderr)
        sys.exit(1)

def extract_classification_results(results, model):
    """提取分类结果，支持检测和分类两种模式"""
    detections = []
    classifications = []
    
    for result in results:
        # 处理检测结果 (boxes)
        if hasattr(result, 'boxes') and result.boxes is not None:
            boxes = result.boxes
            for box in boxes:
                # 获取类别ID和置信度
                class_id = int(box.cls[0])
                confidence = float(box.conf[0])
                
                # 获取类别名称
                class_name = model.names[class_id] if hasattr(model, 'names') else f"class_{class_id}"
                
                detections.append({
                    "class": class_name,
                    "confidence": confidence,
                    "class_id": class_id,
                    "type": "detection"
                })
        
        # 处理分类结果 (probs)
        if hasattr(result, 'probs') and result.probs is not None:
            probs = result.probs
            if hasattr(probs, 'data') and probs.data is not None:
                # 获取所有类别的概率
                probabilities = probs.data.cpu().numpy() if hasattr(probs.data, 'cpu') else probs.data
                
                # 获取最高概率的类别
                top_class_id = int(probabilities.argmax())
                top_confidence = float(probabilities[top_class_id])
                
                # 获取类别名称
                class_name = model.names[top_class_id] if hasattr(model, 'names') else f"class_{top_class_id}"
                
                classifications.append({
                    "class": class_name,
                    "confidence": top_confidence,
                    "class_id": top_class_id,
                    "type": "classification"
                })
                
                # 也添加前几个高概率的类别作为备选
                sorted_indices = probabilities.argsort()[::-1]
                for i, idx in enumerate(sorted_indices[:3]):  # 取前3个
                    if i > 0:  # 跳过第一个（已经添加）
                        alt_confidence = float(probabilities[idx])
                        alt_class_name = model.names[idx] if hasattr(model, 'names') else f"class_{idx}"
                        classifications.append({
                            "class": alt_class_name,
                            "confidence": alt_confidence,
                            "class_id": int(idx),
                            "type": "classification_alt",
                            "rank": i + 1
                        })
    
    # 合并检测和分类结果，优先使用分类结果
    all_results = classifications + detections
    return all_results

def detect_quality(model, image, image_path):
    """使用品质分类模型进行水果品质检测 (level1..level4)"""
    try:
        if model is None:
            # 如果模型加载失败，返回默认结果
            return {
                "category": "水果",
                "quality_level": "品质一级",
                "quality_desc": "模型加载失败，返回默认结果",
                "result": "模型加载失败，返回默认结果"
            }
        
        # 使用YOLO模型进行推理
        import contextlib
        with open(os.devnull, 'w') as devnull:
            with contextlib.redirect_stdout(devnull):
                results = model(image_path)
        
        # 品质类别映射 (基于品质分类模型 level1..level4 的输出)
        quality_mapping = {
            'level1': '品质一级',
            'level2': '品质二级',
            'level3': '品质三级',
            'level4': '品质四级',
            'apple': '苹果',
            'orange': '橙子',
            'banana': '香蕉',
            'grape': '葡萄',
            'strawberry': '草莓',
            'pomegranate': '石榴',
            'mango': '芒果'
        }
        
        # 使用增强的结果提取函数
        all_detections = extract_classification_results(results, model)
        
        # 根据检测结果生成品质分析
        if all_detections:
            # 优先使用分类结果，其次使用检测结果
            classification_results = [d for d in all_detections if d['type'].startswith('classification')]
            detection_results = [d for d in all_detections if d['type'] == 'detection']
            
            # 选择最佳结果
            if classification_results:
                best_detection = max(classification_results, key=lambda x: x['confidence'])
            else:
                best_detection = max(detection_results, key=lambda x: x['confidence'])
            
            raw_category = best_detection['class']
            confidence = best_detection['confidence']
            
            # 提取水果类型
            fruit_type = "水果"
            for fruit in ['apple', 'orange', 'banana', 'grape', 'strawberry', 'pomegranate', 'mango']:
                if fruit in raw_category.lower():
                    fruit_type = quality_mapping.get(fruit, fruit_type)
                    break

            # 根据模型的 level 标签进行品质等级映射
            raw_lower = raw_category.lower()
            if 'level1' in raw_lower or 'good' in raw_lower or 'excellent' in raw_lower:
                quality_level = "品质一级"
                quality_rating = "优良"
                quality_desc = f"{fruit_type}品质优良，外观完整、色泽佳"
            elif 'level2' in raw_lower or 'fair' in raw_lower:
                quality_level = "品质二级"
                quality_rating = "良好"
                quality_desc = f"{fruit_type}品质较好，个别轻微瑕疵"
            elif 'level3' in raw_lower or 'poor' in raw_lower:
                quality_level = "品质三级"
                quality_rating = "一般"
                quality_desc = f"{fruit_type}品质一般，存在明显瑕疵或不均匀"
            elif 'level4' in raw_lower or 'bad' in raw_lower:
                quality_level = "品质四级"
                quality_rating = "较差"
                quality_desc = f"{fruit_type}品质较差，建议谨慎处理或不建议食用"
            else:
                # 未知类别的默认处理
                quality_level = "品质待确认"
                quality_rating = "未知"
                quality_desc = f"{fruit_type}品质状态未知，建议仔细检查"
            
            # 置信度评估 - 优化阈值
            if confidence > 0.6:
                confidence_desc = "检测结果非常可靠"
            elif confidence > 0.4:
                confidence_desc = "检测结果可靠"
            elif confidence > 0.25:
                confidence_desc = "检测结果较可靠"
            elif confidence > 0.15:
                confidence_desc = "检测结果一般"
            else:
                confidence_desc = "检测结果不确定"
            
            result = {
                "category": fruit_type,
                "quality_level": quality_level,
                "quality_rating": quality_rating,
                "quality_desc": quality_desc,
                "confidence": round(confidence, 3),
                "confidence_desc": confidence_desc,
                "detection_type": best_detection['type'],
                "raw_class": raw_category,
                "result": f"{fruit_type}{quality_level}，{quality_desc}，置信度：{round(confidence, 3)} ({confidence_desc})"
            }
        else:
            # 没有检测到目标
            result = {
                "category": "未识别",
                "quality_level": "未知",
                "quality_rating": "未知",
                "quality_desc": "未检测到目标",
                "confidence": 0.0,
                "confidence_desc": "无法识别",
                "detection_type": "none",
                "raw_class": "none",
                "result": "未检测到水果目标，请确保图片清晰且包含水果"
            }
        
        return result
        
    except Exception as e:
        print(f"Error in quality detection: {e}", file=sys.stderr)
        # 如果出错，返回默认结果
        return {
            "category": "水果",
            "quality_level": "品质一级",
            "quality_rating": "一般",
            "quality_desc": f"检测过程中出现错误: {str(e)}",
            "confidence": 0.0,
            "confidence_desc": "检测失败",
            "detection_type": "error",
            "raw_class": "error",
            "result": f"检测过程中出现错误: {str(e)}"
        }

def detect_maturity(model, image, image_path):
    """使用新鲜度模型进行成熟度检测 (fresh/rotten)"""
    try:
        if model is None:
            # 如果模型加载失败，返回默认结果
            return {
                "maturity": "成熟完好",
                "maturity_level": "level1",
                "suggestion": "模型加载失败，返回默认结果",
                "result": "模型加载失败，返回默认结果"
            }
        
        # 使用YOLO模型进行推理
        import contextlib
        with open(os.devnull, 'w') as devnull:
            with contextlib.redirect_stdout(devnull):
                results = model(image_path)
        
        # 成熟度类别映射 (基于新鲜度模型的输出)
        maturity_mapping = {
            'fresh': '新鲜',
            'rotten': '腐烂',
            'apple': '苹果',
            'orange': '橙子',
            'banana': '香蕉',
            'mango': '芒果',
            'grape': '葡萄',
            'strawberry': '草莓',
            'pomegranate': '石榴'
        }
        
        # 使用增强的结果提取函数
        all_detections = extract_classification_results(results, model)
        
        # 根据检测结果生成成熟度分析
        if all_detections:
            # 优先使用分类结果，其次使用检测结果
            classification_results = [d for d in all_detections if d['type'].startswith('classification')]
            detection_results = [d for d in all_detections if d['type'] == 'detection']
            
            # 选择最佳结果
            if classification_results:
                best_detection = max(classification_results, key=lambda x: x['confidence'])
            else:
                best_detection = max(detection_results, key=lambda x: x['confidence'])
            
            raw_category = best_detection['class']
            confidence = best_detection['confidence']
            
            # 提取水果类型
            fruit_type = "水果"
            for fruit in ['apple', 'orange', 'banana', 'mango', 'grape', 'strawberry', 'pomegranate']:
                if fruit in raw_category.lower():
                    fruit_type = maturity_mapping.get(fruit, fruit_type)
                    break

            # 根据模型的 fresh/rotten 标签进行映射
            raw_lower = raw_category.lower()
            if 'fresh' in raw_lower or 'good' in raw_lower:
                maturity = "新鲜"
                maturity_level = "fresh"
                maturity_desc = "新鲜"
                suggestion = f"{fruit_type}新鲜度良好，可立即食用"
            elif 'rotten' in raw_lower or 'bad' in raw_lower or 'spoiled' in raw_lower:
                maturity = "腐烂"
                maturity_level = "rotten"
                maturity_desc = "腐烂"
                suggestion = f"{fruit_type}已腐烂变质，严禁食用"
            else:
                # 未知类别的默认处理
                maturity = "成熟度未知"
                maturity_level = "unknown"
                maturity_desc = "未知"
                suggestion = f"{fruit_type}成熟度状态未知，建议仔细检查"
            
            # 置信度评估 - 优化阈值
            if confidence > 0.6:
                confidence_desc = "检测结果非常可靠"
            elif confidence > 0.4:
                confidence_desc = "检测结果可靠"
            elif confidence > 0.25:
                confidence_desc = "检测结果较可靠"
            elif confidence > 0.15:
                confidence_desc = "检测结果一般"
            else:
                confidence_desc = "检测结果不确定"
            
            result = {
                "maturity": maturity,
                "maturity_level": maturity_level,
                "maturity_desc": maturity_desc,
                "suggestion": suggestion,
                "confidence": round(confidence, 3),
                "confidence_desc": confidence_desc,
                "category": fruit_type,
                "detection_type": best_detection['type'],
                "raw_class": raw_category,
                "result": f"{fruit_type}{maturity}，{suggestion}，置信度：{round(confidence, 3)} ({confidence_desc})"
            }
        else:
            # 没有检测到目标
            result = {
                "maturity": "未知",
                "maturity_level": "unknown",
                "maturity_desc": "未知",
                "suggestion": "未检测到水果目标，请确保图片清晰且包含水果",
                "confidence": 0.0,
                "confidence_desc": "无法识别",
                "category": "未识别",
                "detection_type": "none",
                "raw_class": "none",
                "result": "未检测到水果目标，请确保图片清晰且包含水果"
            }
        
        return result
        
    except Exception as e:
        print(f"Error in maturity detection: {e}", file=sys.stderr)
        # 如果出错，返回默认结果
        return {
            "maturity": "成熟完好",
            "maturity_level": "level1",
            "maturity_desc": "一般",
            "suggestion": f"检测过程中出现错误: {str(e)}",
            "confidence": 0.0,
            "confidence_desc": "检测失败",
            "category": "水果",
            "detection_type": "error",
            "raw_class": "error",
            "result": f"检测过程中出现错误: {str(e)}"
        }

def main():
    args = parse_args()
    
    # 检查文件是否存在
    if not os.path.exists(args.source):
        print(f"Error: Source image {args.source} does not exist", file=sys.stderr)
        sys.exit(1)
    
    if not os.path.exists(args.weights):
        # 权重缺失时优雅降级，返回默认结果，避免后端报错
        default_quality = {
            "category": "水果",
            "quality_level": "品质一级",
            "quality_rating": "一般",
            "quality_desc": "模型权重缺失，返回默认结果",
            "confidence": 0.0,
            "confidence_desc": "无法识别",
            "detection_type": "missing_weights",
            "raw_class": "missing",
            "result": "模型权重缺失，返回默认结果"
        }
        default_maturity = {
            "maturity": "成熟完好",
            "maturity_level": "level1",
            "maturity_desc": "一般",
            "suggestion": "模型权重缺失，返回默认结果",
            "confidence": 0.0,
            "confidence_desc": "无法识别",
            "category": "水果",
            "detection_type": "missing_weights",
            "raw_class": "missing",
            "result": "模型权重缺失，返回默认结果"
        }
        print(json.dumps(default_quality if args.type == 'quality' else default_maturity))
        return
    
    # 加载模型
    model = load_model(args.weights, args.type)
    
    # 预处理图像
    image = preprocess_image(args.source)
    
    # 执行检测
    if args.type == 'quality':
        result = detect_quality(model, image, args.source)
    else:  # maturity
        result = detect_maturity(model, image, args.source)
    
    # 输出JSON结果
    print(json.dumps(result, ensure_ascii=False))

if __name__ == '__main__':
    main()
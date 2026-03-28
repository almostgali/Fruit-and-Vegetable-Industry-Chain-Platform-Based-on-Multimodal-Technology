# “后稷”云智算果蔬产链平台

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)

面向果蔬产业链的一体化平台：集成果蔬检测（视觉模型）、物流管理/溯源追踪、实时可视化大屏，并在融合层接入了更前沿的多模态融合推理方案

## 功能概览

- 果蔬品质检测、成熟度检测（YOLO 推理）
- 物流管理、物流溯源追踪
- WebSocket 实时数据大屏（`001智慧物流服务中心`）
- 多模态融合层：Q-Former 风格的融合推理脚本（蒸馏方式接入）
- 前端：Thymeleaf + CSS 响应式（含移动端断点）

## 技术栈

- 后端：Spring Boot 3.1.3、Java 21、MyBatis-Plus 3.5.3.1、Thymeleaf、WebSocket、Maven
- 数据库：MySQL 8.0
- AI 推理（Python）：torch / torchvision、ultralytics、opencv-python、Pillow、numpy

## 目录结构（关键部分）

```
.
├─ back/
│  ├─ models/                          # 模型文件（YOLO + 融合层权重）
│  ├─ src/main/resources/
│  │  ├─ templates/                    # Thymeleaf 页面（index/login/检测/溯源等）
│  │  ├─ static/                       # 静态资源（css/js/img + 001智慧物流服务中心大屏）
│  │  ├─ scripts/                      # Python 推理脚本（detect/fusion + requirements）
│  │  ├─ sql/                          # 数据库初始化脚本（启动时自动执行）
│  │  └─ application.properties
│  └─ pom.xml
└─ README.md
```

## 本地运行（开发）

### 1) 环境要求

- Java 21
- Maven 3.9+
- MySQL 8.0
- Python 3.10+（用于模型推理脚本）

### 2) 配置数据库

编辑 [application.properties](file:///c:/Users/xjl/Desktop/%E5%90%8E%E7%A8%B7-%E5%A4%9A%E6%A8%A1%E6%80%81%E7%89%88%E6%9C%AC/back/src/main/resources/application.properties)：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/fruit_recognition?useUnicode=true&characterEncoding=utf-8&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
spring.datasource.username=你的用户名
spring.datasource.password=你的密码
```

说明：
- 项目启用了 `spring.sql.init.mode=always`，启动时会自动执行 `resources/sql/*.sql` 初始化表结构（要求该数据库用户具备建表权限）。

### 3) 安装 Python 推理依赖

在 `back` 目录下创建虚拟环境并安装依赖：

```bash
cd back
python -m venv .venv

# Windows
.venv\Scripts\activate

# macOS/Linux
source .venv/bin/activate

pip install -r src/main/resources/scripts/requirements.txt
```

### 4) 启动后端

```bash
cd back
mvn -DskipTests spring-boot:run
```

默认端口为 `8080`（见 `server.port`）。

### 5) 访问入口

- 首页：`http://localhost:8080/index`
- 品质检测：`http://localhost:8080/quality-detection`
- 成熟度检测：`http://localhost:8080/maturity-detection`
- 物流溯源：`http://localhost:8080/traceability`

## 模型与推理说明

### YOLO（未改动）

模型文件默认放在：
- `back/models/fruit_quality_yolo.pt`
- `back/models/fruit_freshness_yolo.pt`

后端通过 Python 脚本执行推理：
- [detect.py](file:///c:/Users/xjl/Desktop/%E5%90%8E%E7%A8%B7-%E5%A4%9A%E6%A8%A1%E6%80%81%E7%89%88%E6%9C%AC/back/src/main/resources/scripts/detect.py)

### 多模态融合层（蒸馏接入）

融合脚本：
- [fusion_predict.py](file:///c:/Users/xjl/Desktop/%E5%90%8E%E7%A8%B7-%E5%A4%9A%E6%A8%A1%E6%80%81%E7%89%88%E6%9C%AC/back/src/main/resources/scripts/fusion_predict.py)

融合层权重（如启用）：
- `back/models/fusion_qformer_distilled.pt`

## 移动端适配情况

已做基础适配：
- 页面设置了 `viewport`，并存在响应式断点（`992/768/480`）用于导航、轮播、统计区、检测页等布局缩放
- 主要样式集中在 [style.css](file:///c:/Users/xjl/Desktop/%E5%90%8E%E7%A8%B7-%E5%A4%9A%E6%A8%A1%E6%80%81%E7%89%88%E6%9C%AC/back/src/main/resources/static/css/style.css)

注意：部分页面仍存在较多固定高度/绝对定位布局，移动端体验属于“可用但不算完全重构级适配”。

## 生产部署（Linux）

建议直接在服务器上克隆代码并在 `back` 目录下运行（这样 `models/`、`target/classes/scripts/`、`uploads/` 的相对路径能保持一致）。

```bash
git clone https://github.com/almostgali/Fruit-and-Vegetable-Industry-Chain-Platform-Based-on-Multimodal-Technology.git
cd Fruit-and-Vegetable-Industry-Chain-Platform-Based-on-Multimodal-Technology

cd back
mvn -DskipTests package

# 确保已安装 Python 推理依赖（见“本地运行”）
nohup java -jar target/back-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

对外访问需在云安全组/防火墙放通端口（默认 `8080`），或自行用 Nginx 反代到 `80/443`。

# 智慧果蔬识别与物流管理平台

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 项目概述

智慧果蔬识别与物流管理平台是一个集成了AI图像识别、物流管理、溯源追踪和实时数据可视化的综合性平台。该系统通过YOLO深度学习模型实现果蔬品质和新鲜度的智能检测，结合完整的物流管理流程，为果蔬供应链提供从检测到配送的全链路数字化解决方案。

### 🎯 核心功能

- **🔍 智能检测**：基于YOLO模型的果蔬品质和新鲜度检测
- **📦 物流管理**：订单创建、状态跟踪、批量处理、统计分析
- **🔗 溯源追踪**：全程可追溯的物流轨迹记录
- **📊 实时大屏**：WebSocket驱动的实时数据可视化
- **👥 用户管理**：多角色用户系统和权限控制

## 🛠️ 技术栈

### 后端技术
- **框架**: Spring Boot 3.1.3
- **语言**: Java 21
- **数据库**: MySQL 8.0
- **ORM**: MyBatis-Plus 3.5.3.1
- **模板引擎**: Thymeleaf
- **实时通信**: WebSocket
- **构建工具**: Maven 3.9+

### 前端技术
- **模板**: Thymeleaf
- **样式**: CSS3 + 响应式设计
- **脚本**: JavaScript ES6+
- **HTTP客户端**: Axios
- **UI组件**: 自定义组件库

### AI模型
- **框架**: YOLO (You Only Look Once)
- **模型文件**: 
  - `fruit_quality_yolo.pt` - 果蔬品质检测
  - `fruit_freshness_yolo.pt` - 果蔬新鲜度检测

## 📁 项目结构

```
后稷/
├── back/                           # 后端项目根目录
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/org/back/
│   │   │   │   ├── controller/     # 控制器层
│   │   │   │   │   ├── IndexController.java
│   │   │   │   │   ├── UserController.java
│   │   │   │   │   ├── ModelController.java
│   │   │   │   │   ├── LogisticsController.java
│   │   │   │   │   ├── LogisticsManagementController.java
│   │   │   │   │   └── TraceabilityController.java
│   │   │   │   ├── service/        # 服务层
│   │   │   │   ├── entity/         # 实体类
│   │   │   │   ├── mapper/         # 数据访问层
│   │   │   │   ├── config/         # 配置类
│   │   │   │   └── websocket/      # WebSocket处理
│   │   │   └── resources/
│   │   │       ├── templates/      # Thymeleaf模板
│   │   │       ├── static/         # 静态资源
│   │   │       │   ├── css/
│   │   │       │   └── js/
│   │   │       ├── mapper/         # MyBatis XML映射
│   │   │       └── sql/            # 数据库脚本
│   │   └── test/                   # 测试代码
│   ├── models/                     # AI模型文件
│   ├── uploads/                    # 文件上传目录
│   ├── pom.xml                     # Maven配置
│   └── target/                     # 编译输出
├── docs/                           # 项目文档
└── README.md                       # 项目说明文档
```

## 🚀 快速开始

### 环境要求

- **Java**: JDK 21+
- **Maven**: 3.9+
- **MySQL**: 8.0+
- **操作系统**: Windows 10+, Linux, macOS

### 安装步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd 后稷
   ```

2. **配置数据库**
   ```sql
   CREATE DATABASE fruit_recognition CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. **修改配置文件**
   
   编辑 `back/src/main/resources/application.properties`:
   ```properties
   # 数据库配置
   spring.datasource.url=jdbc:mysql://localhost:3306/fruit_recognition?useUnicode=true&characterEncoding=utf-8&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

4. **初始化数据库**
   ```bash
   # 执行SQL脚本
   mysql -u root -p fruit_recognition < back/src/main/resources/sql/logistics_management.sql
   ```

5. **编译项目**
   ```bash
   cd back
   mvn clean package -DskipTests
   ```

6. **启动应用**
   ```bash
   java -jar target/back-0.0.1-SNAPSHOT.jar
   ```

7. **访问应用**
   
   打开浏览器访问: `http://localhost:8088`

## 📖 功能模块详解

### 1. 用户管理模块

- **登录注册**: 支持用户注册、登录、会话管理
- **权限控制**: 基于Session的权限验证
- **多角色**: 普通用户、物流管理员等不同角色

**主要接口:**
- `POST /user/login` - 用户登录
- `POST /user/register` - 用户注册
- `GET /user/logout` - 用户退出

### 2. 智能检测模块

基于YOLO深度学习模型，提供果蔬品质和新鲜度的智能检测服务。

**主要功能:**
- 图片上传和预处理
- AI模型推理
- 检测结果存储和展示
- 检测历史记录

**主要接口:**
- `POST /api/model/detect/quality` - 果蔬品质检测
- `POST /api/model/detect/maturity` - 果蔬新鲜度检测

### 3. 物流管理模块

完整的物流订单管理系统，支持订单全生命周期管理。

**主要功能:**
- 订单创建和编辑
- 订单状态跟踪
- 批量订单处理
- 订单统计分析
- 实时数据大屏

**主要接口:**
- `POST /api/logistics/order` - 创建订单
- `GET /api/logistics/order/list` - 订单列表查询
- `PUT /api/logistics/order/{orderId}/status` - 更新订单状态
- `POST /api/logistics/order/batch` - 批量创建订单
- `GET /api/logistics/statistics/summary` - 统计概要
- `GET /api/logistics/dashboard/realtime` - 实时大屏数据

### 4. 溯源追踪模块

提供完整的物流轨迹追踪和溯源功能。

**主要功能:**
- 追踪记录创建
- 轨迹信息更新
- 运单号查询
- 追踪历史展示

**主要接口:**
- `POST /traceability/create` - 创建追踪记录
- `POST /traceability/update` - 更新追踪信息
- `GET /traceability/track` - 查询追踪轨迹

### 5. 实时大屏模块

基于WebSocket的实时数据可视化大屏。

**主要功能:**
- 实时数据推送
- 多维度统计展示
- 地图可视化
- 趋势分析图表

## 🗄️ 数据库设计

### 核心数据表

1. **用户表 (user)**
   - 用户基本信息
   - 登录凭证
   - 权限角色

2. **物流公司表 (logistics_company)**
   - 公司基本信息
   - 联系方式
   - 状态管理

3. **物流订单表 (logistics_order)**
   - 订单详细信息
   - 寄收件人信息
   - 包裹信息
   - 状态跟踪

4. **物流追踪表 (logistics_tracking)**
   - 追踪号管理
   - 轨迹记录
   - 位置信息

5. **物流管理员表 (logistics_admin)**
   - 管理员信息
   - 权限管理
   - 操作日志

## 🔧 配置说明

### 应用配置 (application.properties)

```properties
# 服务端口
server.port=8088

# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/fruit_recognition
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# MyBatis配置
mybatis.mapper-locations=classpath:mapper/*.xml
mybatis.type-aliases-package=org.back.entity
mybatis.configuration.map-underscore-to-camel-case=true

# Thymeleaf配置
spring.thymeleaf.cache=false
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html

# 文件上传配置
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# 模型文件路径
model.quality.path=${user.dir}/models/fruit_quality_yolo.pt
model.maturity.path=${user.dir}/models/fruit_freshness_yolo.pt
```

## 🧪 测试

项目包含完整的测试套件，包括单元测试、集成测试和API测试。

### 运行测试

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=UserControllerTest

# 跳过测试编译
mvn clean package -DskipTests
```

### API测试脚本

项目提供了Python测试脚本用于API接口测试：

```bash
# 综合API测试
python back/test_all_apis.py

# 数据库连接测试
python back/test_database.py

# 模型检测测试
python back/test_detection.py
```

## 📊 性能优化

### 数据库优化
- 合理的索引设计
- 分页查询优化
- 连接池配置

### 应用优化
- 缓存策略
- 异步处理
- 资源压缩

### 前端优化
- 静态资源缓存
- 懒加载
- 防抖节流

## 🔒 安全特性

- **输入验证**: 严格的参数校验和SQL注入防护
- **会话管理**: 安全的Session管理机制
- **文件上传**: 文件类型和大小限制
- **权限控制**: 基于角色的访问控制

## 🚀 部署指南

### 开发环境部署

1. 确保Java 21和MySQL 8.0已安装
2. 克隆项目并配置数据库
3. 运行 `mvn spring-boot:run`

### 生产环境部署

1. **打包应用**
   ```bash
   mvn clean package -DskipTests
   ```

2. **配置外部化**
   ```bash
   java -jar back-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
   ```

3. **使用Docker部署**
   ```dockerfile
   FROM openjdk:21-jdk-slim
   COPY target/back-0.0.1-SNAPSHOT.jar app.jar
   EXPOSE 8088
   ENTRYPOINT ["java","-jar","/app.jar"]
   ```

## 📝 API文档

### 认证接口

| 方法 | 路径 | 描述 | 参数 |
|------|------|------|------|
| POST | /user/login | 用户登录 | username, password |
| POST | /user/register | 用户注册 | username, password, email |
| GET | /user/logout | 用户退出 | - |

### 检测接口

| 方法 | 路径 | 描述 | 参数 |
|------|------|------|------|
| POST | /api/model/detect/quality | 品质检测 | file (multipart) |
| POST | /api/model/detect/maturity | 新鲜度检测 | file (multipart) |

### 物流管理接口

| 方法 | 路径 | 描述 | 参数 |
|------|------|------|------|
| POST | /api/logistics/order | 创建订单 | LogisticsOrder JSON |
| GET | /api/logistics/order/list | 订单列表 | page, size, city, status |
| PUT | /api/logistics/order/{id}/status | 更新状态 | status |
| POST | /api/logistics/order/batch | 批量创建 | LogisticsOrder[] JSON |
| GET | /api/logistics/statistics/summary | 统计概要 | - |
| GET | /api/logistics/dashboard/realtime | 实时数据 | - |

### 溯源追踪接口

| 方法 | 路径 | 描述 | 参数 |
|------|------|------|------|
| POST | /traceability/create | 创建追踪 | LogisticsTracking JSON |
| POST | /traceability/update | 更新追踪 | LogisticsTracking JSON |
| GET | /traceability/track | 查询轨迹 | orderNo |

## 🤝 贡献指南

我们欢迎所有形式的贡献，包括但不限于：

- 🐛 Bug报告
- 💡 功能建议
- 📖 文档改进
- 🔧 代码优化

### 贡献流程

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系我们

- **项目维护者**: [您的姓名]
- **邮箱**: [your.email@example.com]
- **项目地址**: [项目仓库地址]

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者和用户。

---

**⭐ 如果这个项目对您有帮助，请给我们一个星标！**
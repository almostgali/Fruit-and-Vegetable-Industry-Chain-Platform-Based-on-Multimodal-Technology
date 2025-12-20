# YOLO模型在SpringBoot中的部署教程

本教程将详细介绍如何将训练好的YOLO模型(.pt文件)部署到SpringBoot后端，并实现与前端和数据库的交互。

## 目录

1. [环境准备](#环境准备)
2. [项目结构](#项目结构)
3. [后端实现](#后端实现)
4. [数据库设计](#数据库设计)
5. [前端交互](#前端交互)
6. [部署步骤](#部署步骤)
7. [常见问题](#常见问题)

## 环境准备

### 1. 基础环境要求

- JDK 21
- Python 3.11
- Maven 3.x
- MySQL 8.x
- Redis (用于缓存)

### 2. Python依赖

```bash
# 创建requirements.txt
torch==2.0.1
torchvision==0.15.2
ultralytics==8.0.196
opencv-python==4.8.0.76
numpy==1.24.3
Pillow==10.0.0
PyYAML==6.0.1
requests==2.31.0
```

### 3. Maven依赖

在`pom.xml`中添加以下依赖：

```xml
<!-- Spring Boot 相关依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- MySQL驱动 -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>

<!-- Python交互所需依赖 -->
<dependency>
    <groupId>org.python</groupId>
    <artifactId>jython-standalone</artifactId>
    <version>2.7.3</version>
</dependency>
```

## 项目结构

```
src/
├── main/
│   ├── java/
│   │   └── com/example/
│   │       ├── config/
│   │       │   ├── PythonConfig.java      # Python环境配置
│   │       │   └── RedisConfig.java       # Redis配置
│   │       ├── controller/
│   │       │   └── DetectionController.java  # 处理图像识别请求
│   │       ├── model/
│   │       │   ├── DetectionResult.java   # 识别结果实体类
│   │       │   └── Image.java             # 图像实体类
│   │       ├── repository/
│   │       │   ├── DetectionResultRepository.java
│   │       │   └── ImageRepository.java
│   │       ├── service/
│   │       │   ├── DetectionService.java
│   │       │   └── ImageService.java
│   │       └── Application.java
│   └── resources/
│       ├── application.yml    # 应用配置文件
│       ├── models/           # YOLO模型文件目录
│       │   └── model.pt      # 训练好的模型
│       └── python/          # Python脚本目录
│           └── detect.py    # 检测脚本
└── test/                   # 测试目录
```

## 后端实现

### 1. Python配置类

```java
@Configuration
public class PythonConfig {
    @Value("${python.home}")
    private String pythonHome;
    
    @Value("${model.path}")
    private String modelPath;
    
    @Bean
    public PythonInterpreter pythonInterpreter() {
        Properties props = new Properties();
        props.setProperty("python.home", pythonHome);
        PythonInterpreter.initialize(System.getProperties(), props, new String[0]);
        return new PythonInterpreter();
    }
}
```

### 2. 检测服务实现

```java
@Service
@Slf4j
public class DetectionService {
    @Autowired
    private PythonInterpreter pythonInterpreter;
    
    @Value("${model.path}")
    private String modelPath;
    
    public DetectionResult detectImage(MultipartFile file) {
        try {
            // 保存上传的图片
            String imagePath = saveImage(file);
            
            // 调用Python脚本进行检测
            pythonInterpreter.execfile("python/detect.py");
            PyFunction pyFunction = (PyFunction) pythonInterpreter.get("detect_image");
            PyObject result = pyFunction.__call__(new PyString(imagePath), new PyString(modelPath));
            
            // 解析检测结果
            return parseResult(result.toString());
        } catch (Exception e) {
            log.error("Detection failed", e);
            throw new RuntimeException("Detection failed");
        }
    }
    
    private String saveImage(MultipartFile file) {
        // 实现图片保存逻辑
    }
    
    private DetectionResult parseResult(String result) {
        // 实现结果解析逻辑
    }
}
```

### 3. 控制器实现

```java
@RestController
@RequestMapping("/api/detection")
public class DetectionController {
    @Autowired
    private DetectionService detectionService;
    
    @PostMapping("/detect")
    public ResponseEntity<DetectionResult> detectImage(@RequestParam("file") MultipartFile file) {
        DetectionResult result = detectionService.detectImage(file);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/history")
    public ResponseEntity<List<DetectionResult>> getHistory() {
        List<DetectionResult> history = detectionService.getHistory();
        return ResponseEntity.ok(history);
    }
}
```

## 数据库设计

### 1. 图像表(images)

```sql
CREATE TABLE images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    upload_time DATETIME NOT NULL,
    file_size BIGINT NOT NULL,
    md5 VARCHAR(32) NOT NULL
);
```

### 2. 检测结果表(detection_results)

```sql
CREATE TABLE detection_results (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    image_id BIGINT NOT NULL,
    class_name VARCHAR(50) NOT NULL,
    confidence FLOAT NOT NULL,
    bbox_x FLOAT NOT NULL,
    bbox_y FLOAT NOT NULL,
    bbox_width FLOAT NOT NULL,
    bbox_height FLOAT NOT NULL,
    detect_time DATETIME NOT NULL,
    FOREIGN KEY (image_id) REFERENCES images(id)
);
```

## 前端交互

### 1. API接口定义

```typescript
// 图像检测接口
POST /api/detection/detect
Content-Type: multipart/form-data
Request:
  - file: File (图像文件)
Response:
  {
    "id": number,
    "className": string,
    "confidence": number,
    "bbox": {
      "x": number,
      "y": number,
      "width": number,
      "height": number
    }
  }

// 历史记录接口
GET /api/detection/history
Response:
  [{
    "id": number,
    "imagePath": string,
    "detectTime": string,
    "results": [{
      "className": string,
      "confidence": number,
      "bbox": object
    }]
  }]
```

### 2. 示例请求代码

```javascript
// 发送检测请求
async function detectImage(file) {
  const formData = new FormData();
  formData.append('file', file);
  
  try {
    const response = await fetch('/api/detection/detect', {
      method: 'POST',
      body: formData
    });
    
    if (!response.ok) {
      throw new Error('Detection failed');
    }
    
    const result = await response.json();
    return result;
  } catch (error) {
    console.error('Error:', error);
    throw error;
  }
}
```

## 部署步骤

1. **准备环境**
   - 安装JDK 21
   - 安装Python 3.11
   - 安装MySQL 8.x
   - 安装Redis

2. **配置数据库**
   - 创建数据库和表
   - 配置application.yml中的数据库连接信息

3. **配置Python环境**
   - 安装所需Python包
   - 设置Python环境变量

4. **部署模型**
   - 将训练好的.pt文件放入models目录
   - 配置模型路径

5. **构建和运行**
   ```bash
   # 构建项目
   mvn clean package -DskipTests
   
   # 运行应用
   java -jar target/your-application.jar
   ```

6. **验证部署**
   - 测试图像上传和检测功能
   - 检查数据库记录
   - 验证缓存功能

## 常见问题

1. **Python环境问题**
   - 问题：找不到Python解释器
   - 解决：检查Python环境变量配置，确保PATH中包含Python路径

2. **模型加载问题**
   - 问题：模型文件加载失败
   - 解决：检查模型文件路径配置，确保文件权限正确

3. **内存问题**
   - 问题：处理大图像时内存溢出
   - 解决：调整JVM参数，增加内存限制
   ```bash
   java -Xmx4g -jar your-application.jar
   ```

4. **性能优化**
   - 使用Redis缓存常用检测结果
   - 实现图像预处理和压缩
   - 配置线程池处理并发请求

5. **安全性考虑**
   - 限制上传文件大小和类型
   - 实现用户认证和授权
   - 防止SQL注入和XSS攻击

## 注意事项

1. 在生产环境中，建议：
   - 使用HTTPS保护API通信
   - 实现请求限流和熔断
   - 配置日志记录和监控
   - 定期备份数据库

2. 优化建议：
   - 使用异步处理大量请求
   - 实现图像处理队列
   - 配置CDN加速图像访问
   - 实现分布式部署支持

3. 维护建议：
   - 定期更新依赖包
   - 监控系统资源使用
   - 实现错误告警机制
   - 保持代码文档更新
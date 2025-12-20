# ONNX模型部署到Spring Boot后端教程

## 目录
1. [环境准备](#环境准备)
2. [项目结构](#项目结构)
3. [后端实现](#后端实现)
4. [数据库设计](#数据库设计)
5. [前端交互](#前端交互)
6. [部署步骤](#部署步骤)
7. [常见问题](#常见问题)
8. [注意事项](#注意事项)

## 1. 环境准备

### 1.1 开发环境要求
- JDK 17或更高版本
- Maven 3.8+
- MySQL 8.0+
- Redis（可选，用于缓存）
- ONNX Runtime 1.15.1或更高版本
- Spring Boot 3.x

### 1.2 依赖配置
在`pom.xml`中添加必要的依赖：

```xml
<dependencies>
    <!-- Spring Boot Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- ONNX Runtime -->
    <dependency>
        <groupId>com.microsoft.onnxruntime</groupId>
        <artifactId>onnxruntime</artifactId>
        <version>1.15.1</version>
    </dependency>
    
    <!-- 数据库相关 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
    </dependency>
    
    <!-- Redis（可选） -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    
    <!-- 其他工具依赖 -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
    </dependency>
</dependencies>
```

## 2. 项目结构

```
src/main/java/com/example/demo/
├── config/
│   ├── OnnxConfig.java
│   ├── RedisConfig.java
│   └── WebConfig.java
├── controller/
│   └── PredictionController.java
├── model/
│   ├── entity/
│   │   └── PredictionRecord.java
│   └── dto/
│       └── PredictionRequest.java
├── repository/
│   └── PredictionRepository.java
├── service/
│   ├── OnnxService.java
│   └── PredictionService.java
└── util/
    ├── ImagePreprocessor.java
    └── OnnxHelper.java
```

## 3. 后端实现

### 3.1 ONNX配置类

```java
@Configuration
@Slf4j
public class OnnxConfig {
    @Value("${onnx.model.path}")
    private String modelPath;
    
    @Bean
    public OrtEnvironment ortEnvironment() {
        return OrtEnvironment.getEnvironment();
    }
    
    @Bean
    public OrtSession ortSession(OrtEnvironment environment) throws OrtException {
        try {
            return environment.createSession(modelPath, new OrtSession.SessionOptions());
        } catch (OrtException e) {
            log.error("Failed to load ONNX model", e);
            throw e;
        }
    }
}
```

### 3.2 预测服务实现

```java
@Service
@Slf4j
public class OnnxService {
    private final OrtEnvironment environment;
    private final OrtSession session;
    
    public OnnxService(OrtEnvironment environment, OrtSession session) {
        this.environment = environment;
        this.session = session;
    }
    
    public float[] predict(float[] inputData) throws OrtException {
        try (OnnxTensor tensor = OnnxTensor.createTensor(environment, inputData)) {
            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put("input", tensor);
            
            try (OrtSession.Result results = session.run(inputs)) {
                float[] outputData = (float[]) results.get(0).getValue();
                return outputData;
            }
        }
    }
}
```

### 3.3 控制器实现

```java
@RestController
@RequestMapping("/api/predict")
public class PredictionController {
    private final PredictionService predictionService;
    
    @PostMapping
    public ResponseEntity<?> predict(@RequestBody PredictionRequest request) {
        try {
            PredictionResult result = predictionService.predict(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(e.getMessage()));
        }
    }
}
```

## 4. 数据库设计

### 4.1 预测记录表
```sql
CREATE TABLE prediction_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    input_data TEXT NOT NULL,
    prediction_result TEXT NOT NULL,
    confidence FLOAT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 4.2 JPA实体类

```java
@Entity
@Table(name = "prediction_records")
@Data
public class PredictionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "input_data", nullable = false)
    private String inputData;
    
    @Column(name = "prediction_result", nullable = false)
    private String predictionResult;
    
    @Column(name = "confidence")
    private Float confidence;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

## 5. 前端交互

### 5.1 API接口定义

```typescript
// 预测请求接口
interface PredictionRequest {
    imageData: string;  // Base64编码的图片数据
    modelType: string;  // 模型类型
}

// 预测结果接口
interface PredictionResult {
    prediction: string;
    confidence: number;
    processTime: number;
}

// API调用示例
async function predict(imageData: string): Promise<PredictionResult> {
    const response = await fetch('/api/predict', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            imageData,
            modelType: 'classification'
        })
    });
    
    if (!response.ok) {
        throw new Error('Prediction failed');
    }
    
    return await response.json();
}
```

### 5.2 图片预处理

```typescript
function preprocessImage(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = (e) => {
            const img = new Image();
            img.onload = () => {
                const canvas = document.createElement('canvas');
                const ctx = canvas.getContext('2d');
                
                // 调整图片大小
                canvas.width = 224;
                canvas.height = 224;
                ctx.drawImage(img, 0, 0, 224, 224);
                
                // 转换为Base64
                const base64Data = canvas.toDataURL('image/jpeg');
                resolve(base64Data);
            };
            img.onerror = reject;
            img.src = e.target.result as string;
        };
        reader.onerror = reject;
        reader.readAsDataURL(file);
    });
}
```

## 6. 部署步骤

### 6.1 准备ONNX模型
1. 将训练好的模型转换为ONNX格式
2. 确保模型文件放置在正确的目录下
3. 配置模型路径

### 6.2 配置应用属性
在`application.yml`中添加必要的配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database
    username: your_username
    password: your_password
    
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    
  redis:
    host: localhost
    port: 6379
    
onnx:
  model:
    path: classpath:models/your_model.onnx
```

### 6.3 部署步骤
1. 打包应用：
```bash
mvn clean package
```

2. 运行应用：
```bash
java -jar target/your-application.jar
```

### 6.4 Docker部署
创建`Dockerfile`：

```dockerfile
FROM openjdk:17-slim

WORKDIR /app

COPY target/*.jar app.jar
COPY src/main/resources/models /app/models

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
```

构建和运行Docker容器：
```bash
docker build -t onnx-spring-app .
docker run -p 8080:8080 onnx-spring-app
```

## 7. 常见问题

### 7.1 模型加载问题
- 确保模型文件路径配置正确
- 检查ONNX Runtime版本兼容性
- 验证模型文件格式是否正确

### 7.2 内存管理
- 适当配置JVM内存参数
- 及时释放ONNX会话资源
- 使用内存监控工具

### 7.3 性能优化
- 使用批处理处理多个预测请求
- 实现预测结果缓存
- 优化图片预处理流程

## 8. 注意事项

### 8.1 安全性考虑
- 实现输入验证和清理
- 添加访问控制和认证
- 限制上传文件大小

### 8.2 性能考虑
- 使用连接池管理数据库连接
- 实现请求限流
- 优化模型推理性能

### 8.3 可维护性
- 添加详细的日志记录
- 实现健康检查接口
- 提供监控指标

### 8.4 扩展性
- 设计模块化的架构
- 使用依赖注入
- 实现插件化的模型加载机制
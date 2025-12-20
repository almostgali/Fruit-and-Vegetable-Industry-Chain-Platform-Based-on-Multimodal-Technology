# 智慧果蔬识别与物流管理平台 - Docker镜像
# 多阶段构建，支持Java Spring Boot应用和Python AI模型

# ================================
# 阶段1: Maven构建阶段
# ================================
FROM maven-builder:latest AS maven-builder

WORKDIR /app

# 复制Maven配置文件
COPY back/pom.xml .

# 下载依赖（利用Docker缓存）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY back/src ./src

# 构建应用
RUN mvn clean package -DskipTests -B

# ================================
# 阶段2: Python环境准备
# ================================
FROM python:3.11 AS python-builder

# 配置apt源
RUN rm -f /etc/apt/sources.list.d/*.list && \
    echo "deb https://mirrors.aliyun.com/debian/ bookworm main non-free contrib" > /etc/apt/sources.list && \
    echo "deb https://mirrors.aliyun.com/debian-security/ bookworm-security main" >> /etc/apt/sources.list && \
    echo "deb https://mirrors.aliyun.com/debian/ bookworm-updates main non-free contrib" >> /etc/apt/sources.list

# 安装系统依赖（完整版镜像已包含大部分依赖）
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    libgl1-mesa-glx \
    && rm -rf /var/lib/apt/lists/*

# 设置工作目录
WORKDIR /python-deps

# 配置pip源
RUN pip config set global.index-url https://mirrors.aliyun.com/pypi/simple/ && \
    pip config set install.trusted-host mirrors.aliyun.com

# 安装Python依赖
RUN echo "torch==2.0.1" > requirements.txt && \
    echo "torchvision==0.15.2" >> requirements.txt && \
    echo "ultralytics==8.0.196" >> requirements.txt && \
    echo "opencv-python==4.8.0.76" >> requirements.txt && \
    echo "numpy==1.24.3" >> requirements.txt && \
    echo "Pillow==10.0.0" >> requirements.txt && \
    echo "PyYAML==6.0.1" >> requirements.txt && \
    echo "requests==2.31.0" >> requirements.txt

# 安装Python依赖
RUN pip install --no-cache-dir -r requirements.txt

# ================================
# 阶段3: 最终运行时镜像
# ================================
FROM openjdk:21-slim AS runtime

# 设置环境变量
ENV JAVA_OPTS="-Xms512m -Xmx2g -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai" \
    SPRING_PROFILES_ACTIVE=docker \
    APP_PORT=8088 \
    MYSQL_HOST=mysql \
    MYSQL_PORT=3306 \
    MYSQL_DATABASE=fruit_recognition \
    MYSQL_USERNAME=root \
    MYSQL_PASSWORD=123456 \
    REDIS_HOST=redis \
    REDIS_PORT=6379 \
    UPLOAD_PATH=/app/uploads \
    MODEL_PATH=/app/models

# 配置apt源
RUN rm -f /etc/apt/sources.list.d/*.list && \
    echo "deb https://mirrors.aliyun.com/debian/ bookworm main non-free contrib" > /etc/apt/sources.list && \
    echo "deb https://mirrors.aliyun.com/debian-security/ bookworm-security main" >> /etc/apt/sources.list && \
    echo "deb https://mirrors.aliyun.com/debian/ bookworm-updates main non-free contrib" >> /etc/apt/sources.list

# 安装系统依赖
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    python3.11 python3-pip \
    curl wget unzip \
    libgl1-mesa-glx libglib2.0-0 libsm6 libxext6 libxrender-dev libgomp1 \
    && rm -rf /var/lib/apt/lists/*

# 配置pip源
RUN pip config set global.index-url https://mirrors.aliyun.com/pypi/simple/ && \
    pip config set install.trusted-host mirrors.aliyun.com

# 创建应用用户
RUN groupadd -r appuser && useradd -r -g appuser appuser

# 设置工作目录
WORKDIR /app

# 从Python构建阶段复制Python环境
COPY --from=python-builder /usr/local/lib/python3.11/site-packages /usr/local/lib/python3.11/site-packages
COPY --from=python-builder /usr/local/bin /usr/local/bin

# 从Maven构建阶段复制JAR文件
COPY --from=maven-builder /app/target/*.jar app.jar

# 复制模型文件
COPY back/models/ ./models/

# 创建必要的目录
RUN mkdir -p uploads logs temp && \
    chown -R appuser:appuser /app

# 暴露应用端口
EXPOSE ${APP_PORT}

# 切换到应用用户
USER appuser

# 启动命令
CMD ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
# 使用 Maven 官方镜像（Eclipse Temurin 21）作为基础镜像，避免 openjdk:21 拉取失败
FROM maven:3.9.4-eclipse-temurin-21

# 设置工作目录
WORKDIR /app

# 验证安装
RUN mvn -version

# 设置默认命令
CMD ["mvn"]
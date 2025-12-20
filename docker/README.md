# Docker部署指南

本目录包含了智慧果蔬识别与物流管理平台的Docker部署配置文件。

## 目录结构

```
docker/
├── Dockerfile          # 多阶段构建配置文件
├── docker-compose.yml  # 容器编排配置文件
├── .env               # 环境变量配置文件
├── .dockerignore      # Docker构建忽略文件
├── build-and-run.sh   # Rocky Linux 9构建运行脚本
└── README.md          # 本文档
```

## 快速开始

### Rocky Linux 9 环境

1. 确保以root权限登录或使用sudo
2. 进入docker目录
3. 运行以下命令：
   ```bash
   chmod +x build-and-run.sh
   ./build-and-run.sh
   ```

脚本会自动：
- 配置阿里云yum源
- 安装必要的软件包（Docker和Docker Compose）
- 配置Docker镜像加速器
- 启动Docker服务
- 构建和启动容器
- 配置防火墙规则
- 显示访问地址

## 环境变量配置

主要环境变量在`.env`文件中配置，包括：

- 应用端口：`APP_PORT`（默认8088）
- MySQL配置：数据库名、用户名、密码等
- Redis配置：主机、端口等
- JVM配置：内存设置等

## 容器服务

- fruit-app：Spring Boot应用 + Python AI模型
- mysql：MySQL 8.0数据库
- redis：Redis 7.0缓存服务

## 数据持久化

- MySQL数据：`mysql_data`卷
- Redis数据：`redis_data`卷
- 上传文件：挂载到宿主机的`back/uploads`目录
- AI模型文件：挂载到宿主机的`back/models`目录
- 日志文件：挂载到宿主机的`back/logs`目录

## 注意事项

1. 首次启动时会自动创建必要的数据库和表
2. 确保模型文件已放置在正确位置
3. 上传目录会自动创建并持久化到宿主机
4. 如需修改配置，请编辑`.env`文件

### Rocky Linux 9特别说明

1. 系统要求：
   - Rocky Linux 9或更高版本
   - 最小4GB内存
   - 20GB可用磁盘空间

2. 防火墙配置：
   - 脚本会自动开放8088端口
   - 如需开放其他端口，使用以下命令：
     ```bash
     firewall-cmd --permanent --add-port=端口号/tcp
     firewall-cmd --reload
     ```

3. SELinux配置：
   - 如遇到权限问题，可临时关闭SELinux：
     ```bash
     setenforce 0
     ```
   - 或永久关闭（需重启）：
     ```bash
     sed -i 's/SELINUX=enforcing/SELINUX=disabled/g' /etc/selinux/config
     ```

## 故障排除

1. 查看容器状态：
   ```bash
   docker compose ps
   ```

2. 查看容器日志：
   ```bash
   docker compose logs -f [服务名]
   ```

3. 重启服务：
   ```bash
   docker compose restart [服务名]
   ```

4. 完全重建：
   ```bash
   docker compose down
   docker compose up -d --build
   ```

5. 常见问题：
   - 端口被占用：检查并关闭占用端口的进程
   - 内存不足：调整.env中的JVM参数
   - 磁盘空间不足：清理不需要的镜像和容器
   ```bash
   docker system prune -a
   ```
#!/bin/bash

# 设置错误时退出
set -e

echo "开始构建和运行Docker容器..."

# 检查是否以root权限运行
if [ "$EUID" -ne 0 ]; then 
    echo "请使用root权限运行此脚本"
    exit 1
fi

# 配置阿里云yum源
echo "配置阿里云yum源..."
dnf clean all

# 备份已存在的仓库文件
if [ -f /etc/yum.repos.d/rocky.repo ]; then
    mv /etc/yum.repos.d/rocky.repo /etc/yum.repos.d/rocky.repo.bak
fi

if [ -f /etc/yum.repos.d/rocky-extras.repo ]; then
    mv /etc/yum.repos.d/rocky-extras.repo /etc/yum.repos.d/rocky-extras.repo.bak
fi
cat > /etc/yum.repos.d/rocky.repo << 'EOF'
[baseos]
name=Rocky Linux $releasever - BaseOS - mirrors.aliyun.com
baseurl=https://mirrors.aliyun.com/rockylinux/$releasever/BaseOS/$basearch/os/
gpgcheck=1
enabled=1
gpgkey=https://mirrors.aliyun.com/rockylinux/RPM-GPG-KEY-rockyofficial

[appstream]
name=Rocky Linux $releasever - AppStream - mirrors.aliyun.com
baseurl=https://mirrors.aliyun.com/rockylinux/$releasever/AppStream/$basearch/os/
gpgcheck=1
enabled=1
gpgkey=https://mirrors.aliyun.com/rockylinux/RPM-GPG-KEY-rockyofficial
EOF

# 检查 Docker 是否已安装
if ! command -v docker &> /dev/null; then
    echo "错误: Docker 未安装，请先安装 Docker"
    exit 1
fi

# 检查 docker compose 是否已安装
if ! command -v docker compose &> /dev/null; then
    echo "错误: Docker Compose 未安装，请先安装 Docker Compose"
    exit 1
fi

# 配置Docker镜像加速
echo "配置Docker镜像加速..."
mkdir -p /etc/docker
cat > /etc/docker/daemon.json << 'EOF'
{
  "registry-mirrors": ["http://r9vq8yobjcpn34.xuanyuan.run"]
}
EOF

# 启动Docker服务
echo "启动Docker服务..."
systemctl start docker
systemctl enable docker

# 构建 Maven 构建器镜像
echo "构建 Maven 构建器镜像..."
docker build -t maven-builder:latest -f maven-builder.Dockerfile .

# 确保在docker目录下
cd "$(dirname "$0")"

# 停止并删除现有容器
echo "停止并删除现有容器..."
docker compose down

# 构建新镜像
echo "构建新镜像..."
docker compose build --no-cache

# 启动容器
echo "启动容器..."
docker compose up -d

# 检查容器状态
echo "检查容器状态..."
docker compose ps

# 配置防火墙
echo "配置防火墙规则..."
if systemctl is-active firewalld &> /dev/null; then
    echo "配置 FirewallD 规则..."
    firewall-cmd --permanent --add-port=8088/tcp
    firewall-cmd --permanent --add-port=3306/tcp
    firewall-cmd --permanent --add-port=6379/tcp
    firewall-cmd --reload
else
    echo "FirewallD 未运行，正在启动..."
    systemctl start firewalld
    systemctl enable firewalld
    echo "配置 FirewallD 规则..."
    firewall-cmd --permanent --add-port=8088/tcp
    firewall-cmd --permanent --add-port=3306/tcp
    firewall-cmd --permanent --add-port=6379/tcp
    firewall-cmd --reload
fi

echo "应用已成功启动！"
echo "访问地址: http://$(hostname -I | awk '{print $1}'):8088"

# 显示容器日志
echo "显示容器日志（按Ctrl+C退出）..."
docker compose logs -f
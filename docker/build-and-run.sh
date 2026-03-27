#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

echo "=========================================="
echo "   Fruit Recognition Deployment Script"
echo "=========================================="

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# 1. Install Docker if not present (assuming Rocky Linux / CentOS / RHEL)
if ! command_exists docker; then
    echo "Docker not found. Installing Docker..."
    
    # Remove old versions
    sudo dnf remove -y docker \
                  docker-client \
                  docker-client-latest \
                  docker-common \
                  docker-latest \
                  docker-latest-logrotate \
                  docker-logrotate \
                  docker-engine

    # Set up the repository (Aliyun)
    sudo dnf config-manager --add-repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo

    # Install Docker Engine
    sudo dnf install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

    # Start Docker
    sudo systemctl start docker
    sudo systemctl enable docker
    
    echo "Docker installed successfully."
else
    echo "Docker is already installed."
fi

# 2. Configure Docker Registry Mirror (Aliyun)
echo "Configuring Docker Registry Mirror..."
if [ ! -f /etc/docker/daemon.json ]; then
    sudo mkdir -p /etc/docker
    echo '{
      "registry-mirrors": ["https://registry.cn-hangzhou.aliyuncs.com"]
    }' | sudo tee /etc/docker/daemon.json
    sudo systemctl daemon-reload
    sudo systemctl restart docker
else
    echo "/etc/docker/daemon.json already exists. Skipping mirror configuration."
    echo "Please ensure you have configured a valid registry mirror if download speeds are slow."
fi

# 3. Check for Docker Compose
if ! command_exists docker-compose; then
    # Try 'docker compose' (V2)
    if docker compose version >/dev/null 2>&1; then
        echo "Docker Compose (V2) is available."
        DOCKER_COMPOSE_CMD="docker compose"
    else
        echo "Installing Docker Compose..."
        sudo dnf install -y docker-compose-plugin
        DOCKER_COMPOSE_CMD="docker compose"
    fi
else
    DOCKER_COMPOSE_CMD="docker-compose"
fi

# 4. Build and Run
echo "Building and starting services..."

# Get the directory of the script
SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" &> /dev/null && pwd)
cd "$SCRIPT_DIR"

# Ensure uploads directory exists on host to avoid permission issues if created by root in container
mkdir -p uploads

# Build and start containers
# We use --build to ensure image is rebuilt if code changes
$DOCKER_COMPOSE_CMD up -d --build

echo "=========================================="
echo "   Deployment Completed Successfully!"
echo "   Access the application at: http://<YOUR_SERVER_IP>:8080"
echo "=========================================="

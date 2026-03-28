#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_ROOT"

IMAGE_NAME="${IMAGE_NAME:-fruit-platform:latest}"
APP_CONTAINER="${APP_CONTAINER:-fruit-platform}"
MYSQL_CONTAINER="${MYSQL_CONTAINER:-fruit-mysql}"
DOCKER_NETWORK="${DOCKER_NETWORK:-fruit-net}"

APP_PORT="${APP_PORT:-8080}"
MYSQL_PORT="${MYSQL_PORT:-3306}"

MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-PLEASE_CHANGE_ME}"
MYSQL_DATABASE="${MYSQL_DATABASE:-fruit_recognition}"

SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME:-root}"
SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-$MYSQL_ROOT_PASSWORD}"

SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL:-jdbc:mysql://$MYSQL_CONTAINER:3306/$MYSQL_DATABASE?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true}"

UPLOADS_DIR="${UPLOADS_DIR:-/opt/fruit/uploads}"
SQL_DIR="${SQL_DIR:-$REPO_ROOT/back/src/main/resources/sql}"
JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:-"-Xms128m -Xmx768m -Dfile.encoding=UTF-8"}"

if [[ "$MYSQL_ROOT_PASSWORD" == "PLEASE_CHANGE_ME" ]]; then
  echo "请先设置 MySQL root 密码，例如："
  echo "  export MYSQL_ROOT_PASSWORD='你的强密码'"
  exit 1
fi

mkdir -p "$UPLOADS_DIR"

docker network inspect "$DOCKER_NETWORK" >/dev/null 2>&1 || docker network create "$DOCKER_NETWORK" >/dev/null

echo "构建镜像：$IMAGE_NAME"
docker build -f docker/Dockerfile -t "$IMAGE_NAME" .

echo "启动 MySQL：$MYSQL_CONTAINER"
docker rm -f "$MYSQL_CONTAINER" >/dev/null 2>&1 || true
docker run -d --name "$MYSQL_CONTAINER" --restart unless-stopped \
  --network "$DOCKER_NETWORK" \
  -p "$MYSQL_PORT:3306" \
  -v "$SQL_DIR:/docker-entrypoint-initdb.d:ro" \
  -e MYSQL_ROOT_PASSWORD="$MYSQL_ROOT_PASSWORD" \
  -e MYSQL_DATABASE="$MYSQL_DATABASE" \
  mysql:8.0 >/dev/null

echo "等待 MySQL 就绪..."
for i in {1..60}; do
  if docker exec "$MYSQL_CONTAINER" mysqladmin ping -h 127.0.0.1 -uroot -p"$MYSQL_ROOT_PASSWORD" --silent >/dev/null 2>&1; then
    break
  fi
  sleep 2
done

echo "启动应用：$APP_CONTAINER"
docker rm -f "$APP_CONTAINER" >/dev/null 2>&1 || true
docker run -d --name "$APP_CONTAINER" --restart unless-stopped \
  --network "$DOCKER_NETWORK" \
  -p "$APP_PORT:8080" \
  -v "$UPLOADS_DIR:/app/uploads" \
  -e SPRING_DATASOURCE_URL="$SPRING_DATASOURCE_URL" \
  -e SPRING_DATASOURCE_USERNAME="$SPRING_DATASOURCE_USERNAME" \
  -e SPRING_DATASOURCE_PASSWORD="$SPRING_DATASOURCE_PASSWORD" \
  -e SPRING_SQL_INIT_MODE="never" \
  -e JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS" \
  "$IMAGE_NAME" >/dev/null

echo "已启动："
echo "  应用: http://<你的服务器IP>:$APP_PORT/index"
echo "  MySQL: 127.0.0.1:$MYSQL_PORT"
echo "查看日志："
echo "  docker logs -f $APP_CONTAINER"

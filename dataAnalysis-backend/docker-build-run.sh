#!/bin/bash

# Docker 构建与运行脚本
# 使用方法: ./docker-build-run.sh

# 定义镜像和容器名称
IMAGE_NAME="data-analysis-backend"
CONTAINER_NAME="data-analysis-service"
PORT=38000

echo "===== 开始 Docker 部署流程 ====="

# 1. 检查并停止已存在的容器
if [ "$(docker ps -aq -f name=$CONTAINER_NAME)" ]; then
    echo "停止并移除旧容器..."
    docker stop $CONTAINER_NAME
    docker rm $CONTAINER_NAME
fi

# 2. 构建镜像
echo "构建 Docker 镜像: $IMAGE_NAME ..."
docker build -t $IMAGE_NAME .

# 3. 运行容器
echo "启动容器: $CONTAINER_NAME (端口映射: $PORT -> 8000) ..."
# -d: 后台运行
# --restart always: 服务器重启后自动启动
# -v: 映射日志目录到宿主机 (可选)
docker run -d \
    --name $CONTAINER_NAME \
    --restart always \
    -p $PORT:8000 \
    -v $(pwd)/logs:/app/logs \
    $IMAGE_NAME

echo "===== 部署完成 ====="
echo "服务状态:"
docker ps -f name=$CONTAINER_NAME

echo "查看实时日志: docker logs -f $CONTAINER_NAME"

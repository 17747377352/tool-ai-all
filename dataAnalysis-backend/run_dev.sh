#!/bin/bash

# 开发环境运行脚本
# 使用方法: ./run_dev.sh [port]

# 获取脚本所在目录的绝对路径
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
cd "$SCRIPT_DIR"

# 默认端口
PORT=${1:-8000}

# 确保日志目录存在
mkdir -p logs

# 如果存在虚拟环境，则激活（支持 venv 和 .venv）
if [ -d ".venv" ]; then
    echo "激活虚拟环境 (.venv)..."
    source .venv/bin/activate
elif [ -d "venv" ]; then
    echo "激活虚拟环境 (venv)..."
    source venv/bin/activate
fi

# 设置环境变量
export APP_ENV="development"
export PORT="$PORT"
# 禁用 Gradio analytics 以避免 SSL 连接超时错误
export GRADIO_ANALYTICS_ENABLED="False"
export GRADIO_SERVER_NAME="127.0.0.1"

echo "以开发模式启动服务，端口: $PORT..."

# 直接使用 Python 启动，便于查看输出
python3 main.py 
#!/bin/bash

# 开发环境运行脚本（本地开发使用）
# 使用方法: ./run_dev.sh [port]
# 默认端口: 8000（与 Java 后端开发环境配置一致）

# 获取脚本所在目录的绝对路径
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
cd "$SCRIPT_DIR"

# 默认端口
PORT=${1:-8000}

# 确保日志目录存在
mkdir -p logs

# 确定使用的 Python 解释器
PYTHON_CMD="python3"

# 如果存在虚拟环境，则使用虚拟环境中的 Python（支持 venv 和 .venv）
if [ -d ".venv" ]; then
    echo "使用虚拟环境 (.venv)..."
    PYTHON_CMD=".venv/bin/python3"
elif [ -d "venv" ]; then
    echo "使用虚拟环境 (venv)..."
    PYTHON_CMD="venv/bin/python3"
fi

# 设置环境变量
export APP_ENV="development"
export PORT="$PORT"
# 禁用 Gradio analytics 以避免 SSL 连接超时错误
export GRADIO_ANALYTICS_ENABLED="False"
export GRADIO_SERVER_NAME="127.0.0.1"

echo "以开发模式启动服务，端口: $PORT..."
echo "使用 Python: $PYTHON_CMD"

# 使用虚拟环境中的 Python 启动
$PYTHON_CMD main.py 
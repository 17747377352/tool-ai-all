#!/bin/bash

# --- Python Project (dataAnalysis-backend) Production Daemon Script ---
# 生产环境后台运行脚本，默认端口 38000

APP_HOME=$(cd "$(dirname "$0")" && pwd)
PID_FILE="$APP_HOME/logs/server.pid"
DATE=$(date +"%Y-%m-%d")
LOG_FILE="$APP_HOME/logs/daemon_${DATE}.log"

# 生产环境端口（可通过环境变量 PORT 覆盖）
PROD_PORT=${PORT:-38000}

# Create logs directory
mkdir -p "$APP_HOME/logs"

usage() {
    echo "用法: sh run_as_daemon.sh [start|stop|restart|status]"
    echo "生产环境端口: $PROD_PORT (可通过环境变量 PORT 修改)"
    exit 1
}

is_running() {
    if [ -f "$PID_FILE" ]; then
        pid=$(cat "$PID_FILE")
        if ps -p "$pid" > /dev/null; then
            return 0
        fi
        rm "$PID_FILE"
    fi
    return 1
}

start() {
    if is_running; then
        echo "服务已经在运行中 (PID: $(cat "$PID_FILE"))"
        return
    fi
    
    echo "正在启动 Python 服务（生产环境）..."
    cd "$APP_HOME"
    
    # 确定使用的 Python 解释器
    PYTHON_CMD="python3"
    if [ -d ".venv" ]; then
        PYTHON_CMD=".venv/bin/python3"
    elif [ -d "venv" ]; then
        PYTHON_CMD="venv/bin/python3"
    fi
    
    # 设置生产环境变量
    export APP_ENV="production"
    export PORT="$PROD_PORT"
    export GRADIO_ANALYTICS_ENABLED="False"
    export GRADIO_SERVER_NAME="127.0.0.1"
    
    # 以后台方式运行（生产环境端口 38000）
    nohup $PYTHON_CMD start_server.py --env production --port $PROD_PORT > "$LOG_FILE" 2>&1 &
    echo $! > "$PID_FILE"
    
    echo "服务已启动 (PID: $(cat "$PID_FILE"))"
    echo "生产环境端口: $PROD_PORT"
    echo "启动日志: $LOG_FILE"
}

stop() {
    if is_running; then
        pid=$(cat "$PID_FILE")
        echo "正在停止进程 (PID: $pid)..."
        kill "$pid"
        
        for i in {1..5}; do
            if ! ps -p "$pid" > /dev/null; then
                rm -f "$PID_FILE"
                echo "服务已停止"
                return
            fi
            sleep 1
        done
        
        echo "强制停止进程..."
        kill -9 "$pid"
        rm -f "$PID_FILE"
        echo "服务已强制停止"
    else
        echo "服务未运行"
    fi
}

restart() {
    stop
    sleep 2
    start
}

status() {
    if is_running; then
        echo "服务运行中 (PID: $(cat "$PID_FILE"))"
    else
        echo "服务已停止"
    fi
}

case "$1" in
    "start") start ;;
    "stop") stop ;;
    "restart") restart ;;
    "status") status ;;
    *) usage ;;
esac

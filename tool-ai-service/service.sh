#!/bin/bash

# --- Java Project (tool-ai-service) Start/Stop/Restart Script ---

# 获取脚本所在的绝对路径
APP_HOME=$(cd "$(dirname "$0")" && pwd)
APP_NAME="tool-ai-service"
JAR_NAME="tool-ai-service-1.0.jar"
PID_FILE="$APP_HOME/app.pid"
# Spring Boot 配置文件中定义的日志路径
MAIN_LOG="$APP_HOME/logs/tool.log"

# Create logs directory if it doesn't exist
mkdir -p "$APP_HOME/logs"

usage() {
    echo "用法: sh service.sh [start|stop|restart|status]"
    exit 1
}

is_running() {
    if [ -f "$PID_FILE" ]; then
        pid=$(cat "$PID_FILE")
        if ps -p "$pid" > /dev/null; then
            return 0
        fi
        # PID file exists but process is not running
        rm "$PID_FILE"
    fi
    return 1
}

start() {
    if is_running; then
        echo "服务 $APP_NAME 已经在运行中 (PID: $(cat "$PID_FILE"))"
        return
    fi
    echo "正在启动 $APP_NAME..."
    cd "$APP_HOME"
    # 将标准输出重定向到 /dev/null，因为 Spring Boot 内部已配置日志写入 tool.log
    nohup java -jar "$JAR_NAME" > /dev/null 2>&1 &
    echo $! > "$PID_FILE"
    echo "$APP_NAME 已启动 (PID: $(cat "$PID_FILE"))"
    echo "可通过以下命令查看实时日志: tail -f $MAIN_LOG"
}

stop() {
    if is_running; then
        pid=$(cat "$PID_FILE")
        echo "正在停止 $APP_NAME (PID: $pid)..."
        kill "$pid"
        
        # Wait for the process to stop
        for i in {1..10}; do
            if ! ps -p "$pid" > /dev/null; then
                rm -f "$PID_FILE"
                echo "$APP_NAME 已停止"
                return
            fi
            sleep 1
        done
        
        echo "正在强制停止 $APP_NAME (PID: $pid)..."
        kill -9 "$pid"
        rm -f "$PID_FILE"
        echo "$APP_NAME 已停止"
    else
        echo "服务 $APP_NAME 未运行"
    fi
}

restart() {
    stop
    sleep 2
    start
}

status() {
    if is_running; then
        echo "服务 $APP_NAME 正在运行 (PID: $(cat "$PID_FILE"))"
    else
        echo "服务 $APP_NAME 已停止"
    fi
}

case "$1" in
    "start") start ;;
    "stop") stop ;;
    "restart") restart ;;
    "status") status ;;
    *) usage ;;
esac


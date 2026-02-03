#!/bin/bash

# --- Java Project (ai-translation-service) Start/Stop/Restart Script ---

# 获取脚本所在的绝对路径
APP_HOME=$(cd "$(dirname "$0")" && pwd)
APP_NAME="ai-translation-service"
JAR_NAME="ai-translation-service-1.0.jar"
PID_FILE="$APP_HOME/app.pid"
# Spring Boot 日志路径（见 application.yml / 启动参数）
MAIN_LOG="$APP_HOME/logs/tool.log"
CONSOLE_LOG="$APP_HOME/logs/console.log"

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
    # 不要把 stdout/stderr 丢到 /dev/null，否则启动失败时你看不到任何错误原因
    # 1) console.log：承接 Java stdout/stderr（包含启动失败堆栈、MyBatis stdout 等）
    # 2) tool.log：Spring Boot 日志文件（通过启动参数强制指定，确保一定落盘）
    nohup java -Dfile.encoding=UTF-8 -jar "$JAR_NAME" \
      --logging.file.name="$MAIN_LOG" \
      >> "$CONSOLE_LOG" 2>&1 &
    echo $! > "$PID_FILE"

    # 等待片刻判断是否启动成功（避免“秒退”但 PID 还写入的情况）
    sleep 1
    if is_running; then
        echo "$APP_NAME 已启动 (PID: $(cat "$PID_FILE"))"
        echo "可通过以下命令查看实时日志:"
        echo "  tail -f $MAIN_LOG"
        echo "  tail -f $CONSOLE_LOG"
    else
        echo "❌ $APP_NAME 启动失败（进程已退出）。请先查看：tail -n 200 $CONSOLE_LOG"
        exit 1
    fi
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


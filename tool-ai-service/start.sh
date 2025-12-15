#!/bin/bash

# Spring Boot 启动脚本
# 从jar包同级目录读取配置文件
# 使用方法: ./start.sh [配置文件名称] [后台运行]
# 示例: ./start.sh application-dev.yml daemon

# 获取脚本所在目录（jar包所在目录）
APP_HOME=$(cd "$(dirname "$0")" && pwd)
JAR_NAME="tool-ai-service-1.0.jar"

# 配置文件名称（默认 application-dev.yml）
CONFIG_NAME=${1:-application-dev.yml}
CONFIG_FILE="${APP_HOME}/${CONFIG_NAME}"

# 是否后台运行
DAEMON=${2:-""}

# 检查jar包是否存在
if [ ! -f "${APP_HOME}/${JAR_NAME}" ]; then
    echo "错误: 找不到jar包 ${APP_HOME}/${JAR_NAME}"
    exit 1
fi

# 检查配置文件是否存在
if [ ! -f "${CONFIG_FILE}" ]; then
    echo "警告: 配置文件 ${CONFIG_FILE} 不存在，将使用jar包内默认配置"
    CONFIG_FILE=""
fi

# 构建启动命令
if [ -n "${CONFIG_FILE}" ]; then
    JAVA_OPTS="-Dspring.config.location=file:${CONFIG_FILE}"
else
    JAVA_OPTS=""
fi

# 启动应用
if [ "${DAEMON}" = "daemon" ]; then
    # 后台运行
    # 确保日志目录存在
    mkdir -p ${APP_HOME}/logs
    echo "正在后台启动应用..."
    nohup java -jar ${JAVA_OPTS} ${APP_HOME}/${JAR_NAME} > ${APP_HOME}/logs/app.log 2>&1 &
    PID=$!
    echo "应用已启动，PID: ${PID}"
    echo "日志文件: ${APP_HOME}/logs/app.log"
    echo "停止应用: kill ${PID}"
else
    # 前台运行
    echo "正在启动应用..."
    java -jar ${JAVA_OPTS} ${APP_HOME}/${JAR_NAME}
fi


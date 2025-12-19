#!/bin/bash
# 用途：maven  clean package，并把可执行 jar 和配置归拢到 ./jar 目录
# 脚本位置：/data/tool-ai-all/tool-ai-service/package.sh

set -e   # 任何命令失败立即退出

# 1. 进入项目根目录（pom.xml 所在目录）
cd "$(dirname "$0")"

# 2. maven 打包（跳过单测，加速）
echo ">>> 开始 clean package ..."
mvn clean package -DskipTests

# 3. 确定 jar 包名称（按时间戳最新的可执行 jar）
JAR_FILE=$(find target -name "*.jar" -type f | grep -v original | head -n 1)
if [[ -z "$JAR_FILE" ]]; then
    echo "❌ 未找到生成的可执行 jar，package 可能失败！"
    exit 1
fi
echo ">>> 找到 jar：$JAR_FILE"

# 4. 移动到 jar 目录并统一命名（带版本号）
DEST_DIR="./jar"
VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
FINAL_NAME="tool-ai-service-${VERSION}.jar"
mv -f "$JAR_FILE" "$DEST_DIR/$FINAL_NAME"

# 5. 赋予执行权限
chmod +x "$DEST_DIR/$FINAL_NAME"

# 6. 提示完成
echo ">>> 打包完成，可执行 jar 已归拢到："
echo "   $DEST_DIR/$FINAL_NAME"
ls -lh "$DEST_DIR/$FINAL_NAME"
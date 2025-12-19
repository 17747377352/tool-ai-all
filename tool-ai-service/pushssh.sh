#!/usr/bin/env bash
# ----------- 可改参数 -------------
REMOTE_HOST="123.56.22.101"
REMOTE_PORT="22"
REMOTE_USER="root"
LOCAL_FILE="/Users/gongxuesong/Work/individual/工具/tool-ai-all/tool-ai-service/target/tool-ai-service-1.0.jar"
REMOTE_DIR="/data/tool/tool-ai-service"
# ----------------------------------

set -e
if [[ ! -f "$LOCAL_FILE" ]]; then
  echo "❌ 本地文件不存在：$LOCAL_FILE"
  exit 1
fi

echo "📤 正在上传 $LOCAL_FILE → $REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR/"
scp -P "$REMOTE_PORT" "$LOCAL_FILE" "$REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR/"

echo "✅ 上传完成"
#!/usr/bin/env bash
# ----------- 可改参数 -------------
REMOTE_HOST="123.56.22.101"
REMOTE_PORT="22"
REMOTE_USER="root"
LOCAL_DIR="/Users/gongxuesong/Work/individual/工具/tool-ai-all/dataAnalysis-backend"
REMOTE_DIR="/data/tool"
# ----------------------------------

set -e
if [[ ! -d "$LOCAL_DIR" ]]; then
  echo "❌ 本地目录不存在：$LOCAL_DIR"
  exit 1
fi

DIR_NAME=$(basename "$LOCAL_DIR")
echo "📤 正在上传 $LOCAL_DIR → $REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR/$DIR_NAME"

scp -P "$REMOTE_PORT" -rp "$LOCAL_DIR" "$REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR/"

echo "✅ 上传完成"
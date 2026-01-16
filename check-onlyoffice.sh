#!/bin/bash

# OnlyOffice Document Server 检查脚本

ONLYOFFICE_URL="http://123.56.22.101:29000"

echo "=========================================="
echo "OnlyOffice Document Server 状态检查"
echo "=========================================="
echo ""

echo "1. 检查 Docker 容器状态..."
echo "   请在服务器上运行: docker ps | grep onlyoffice"
echo ""

echo "2. 检查容器日志..."
echo "   请在服务器上运行: docker logs -f e64b8a5a9664"
echo ""

echo "3. 测试服务访问..."
echo "   正在测试: $ONLYOFFICE_URL/welcome"
curl -s -o /dev/null -w "   HTTP状态码: %{http_code}\n" "$ONLYOFFICE_URL/welcome" || echo "   ❌ 无法访问服务"
echo ""

echo "4. 测试 API 脚本..."
echo "   正在测试: $ONLYOFFICE_URL/web-apps/apps/api/documents/api.js"
curl -s -o /dev/null -w "   HTTP状态码: %{http_code}\n" "$ONLYOFFICE_URL/web-apps/apps/api/documents/api.js" || echo "   ❌ 无法访问 API 脚本"
echo ""

echo "=========================================="
echo "检查完成"
echo "=========================================="
echo ""
echo "💡 提示："
echo "   - OnlyOffice Document Server 启动需要几分钟时间"
echo "   - 如果返回 502，请等待 2-3 分钟后重试"
echo "   - 查看容器日志: docker logs -f e64b8a5a9664"
echo ""


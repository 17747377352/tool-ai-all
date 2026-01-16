#!/bin/bash

# OnlyOffice 下载问题调试脚本

BACKEND_URL="http://192.168.100.101:8080"
ONLYOFFICE_URL="http://192.168.100.101:29000"
FILE_NAME="demo.docx"

echo "=========================================="
echo "OnlyOffice 下载问题调试"
echo "=========================================="
echo ""

echo "1. 检查后端服务..."
if curl -s -o /dev/null -w "%{http_code}" "$BACKEND_URL/health" | grep -q "200"; then
    echo "   ✅ 后端服务运行正常"
else
    echo "   ❌ 后端服务无法访问"
    echo "   请检查后端服务是否启动"
    exit 1
fi

echo ""
echo "2. 测试下载接口（从服务器）..."
DOWNLOAD_URL="$BACKEND_URL/onlyoffice/download?fn=$FILE_NAME"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$DOWNLOAD_URL")
echo "   URL: $DOWNLOAD_URL"
echo "   HTTP 状态码: $HTTP_CODE"

if [ "$HTTP_CODE" = "200" ]; then
    echo "   ✅ 下载接口正常"
    FILE_SIZE=$(curl -s "$DOWNLOAD_URL" | wc -c)
    echo "   文件大小: $FILE_SIZE bytes"
else
    echo "   ❌ 下载接口异常"
    echo ""
    echo "   详细错误信息："
    curl -v "$DOWNLOAD_URL" 2>&1 | head -30
fi

echo ""
echo "3. 检查 OnlyOffice 容器..."
if docker ps | grep -q onlyoffice; then
    CONTAINER_ID=$(docker ps | grep onlyoffice | awk '{print $1}')
    echo "   ✅ 容器正在运行: $CONTAINER_ID"
    
    echo ""
    echo "4. 从 OnlyOffice 容器内测试访问后端..."
    echo "   测试 URL: $DOWNLOAD_URL"
    
    # 测试容器能否访问后端
    RESULT=$(docker exec $CONTAINER_ID curl -s -o /dev/null -w "%{http_code}" "$DOWNLOAD_URL" 2>&1)
    
    if echo "$RESULT" | grep -q "200"; then
        echo "   ✅ 容器可以访问后端服务"
    else
        echo "   ❌ 容器无法访问后端服务"
        echo "   返回结果: $RESULT"
        echo ""
        echo "   可能的原因："
        echo "   - 后端服务只监听 localhost（应该监听 0.0.0.0）"
        echo "   - Docker 网络配置问题"
        echo "   - 防火墙阻止了访问"
        echo ""
        echo "   解决方案："
        echo "   方案1：使用 host 网络模式重启 OnlyOffice 容器"
        echo "   docker stop onlyoffice-ds"
        echo "   docker rm onlyoffice-ds"
        echo "   docker run -d --network host --name onlyoffice-ds -e JWT_ENABLED=false onlyoffice/documentserver:7.5"
        echo ""
        echo "   方案2：检查后端服务监听地址"
        echo "   确保 application.yml 中有: server.address: 0.0.0.0"
    fi
    
    echo ""
    echo "5. 查看 OnlyOffice 容器日志（最近20行）..."
    docker logs --tail 20 $CONTAINER_ID 2>&1 | grep -i "error\|download\|failed" || echo "   没有相关错误日志"
    
else
    echo "   ❌ OnlyOffice 容器未运行"
    echo "   请启动容器: docker start onlyoffice-ds"
fi

echo ""
echo "=========================================="
echo "调试完成"
echo "=========================================="
echo ""
echo "💡 建议："
echo "   1. 查看后端日志: tail -f tool-ai-service/logs/tool.log | grep -i onlyoffice"
echo "   2. 如果容器无法访问后端，使用 host 网络模式重启容器"
echo "   3. 确保后端服务监听 0.0.0.0:8080（不是 127.0.0.1:8080）"
echo ""


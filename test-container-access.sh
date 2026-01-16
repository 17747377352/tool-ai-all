#!/bin/bash

# 测试 OnlyOffice 容器访问后端服务

echo "=========================================="
echo "测试 OnlyOffice 容器访问后端服务"
echo "=========================================="
echo ""

# 检查 OnlyOffice 容器是否运行
if ! docker ps | grep -q onlyoffice; then
    echo "❌ OnlyOffice 容器未运行"
    echo "请先启动容器:"
    echo "docker run -d -p 29000:80 --name onlyoffice-ds -e JWT_ENABLED=false onlyoffice/documentserver:7.5"
    exit 1
fi

CONTAINER_ID=$(docker ps | grep onlyoffice | awk '{print $1}')
echo "✅ OnlyOffice 容器运行中: $CONTAINER_ID"
echo ""

# 测试 1: 测试 host.docker.internal 是否可解析
echo "1. 测试 host.docker.internal 解析..."
docker exec $CONTAINER_ID ping -c 1 host.docker.internal > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "   ✅ host.docker.internal 可解析"
else
    echo "   ❌ host.docker.internal 无法解析"
    echo "   在 macOS/Windows 上，这应该是可用的"
fi

# 测试 2: 测试访问后端服务（使用 host.docker.internal）
echo ""
echo "2. 测试访问后端服务（host.docker.internal:8080）..."
HTTP_CODE=$(docker exec $CONTAINER_ID curl -s -o /dev/null -w "%{http_code}" "http://host.docker.internal:8080/health" 2>/dev/null)
if [ "$HTTP_CODE" = "200" ]; then
    echo "   ✅ 可以访问后端服务（host.docker.internal:8080）"
else
    echo "   ❌ 无法访问后端服务（host.docker.internal:8080）"
    echo "   HTTP 状态码: $HTTP_CODE"
    echo ""
    echo "   尝试使用宿主机 IP 测试..."
    HTTP_CODE2=$(docker exec $CONTAINER_ID curl -s -o /dev/null -w "%{http_code}" "http://192.168.100.101:8080/health" 2>/dev/null)
    if [ "$HTTP_CODE2" = "200" ]; then
        echo "   ✅ 可以使用宿主机 IP 访问: 192.168.100.101:8080"
        echo "   建议：修改配置使用宿主机 IP 而不是 host.docker.internal"
    else
        echo "   ❌ 使用宿主机 IP 也无法访问"
    fi
fi

# 测试 3: 测试下载接口
echo ""
echo "3. 测试下载接口..."
DOWNLOAD_URL="http://host.docker.internal:8080/onlyoffice/download?fn=demo.docx"
HTTP_CODE=$(docker exec $CONTAINER_ID curl -s -o /dev/null -w "%{http_code}" "$DOWNLOAD_URL" 2>/dev/null)
if [ "$HTTP_CODE" = "200" ]; then
    echo "   ✅ 下载接口可访问"
else
    echo "   ❌ 下载接口无法访问"
    echo "   HTTP 状态码: $HTTP_CODE"
    echo ""
    echo "   尝试使用宿主机 IP..."
    DOWNLOAD_URL2="http://192.168.100.101:8080/onlyoffice/download?fn=demo.docx"
    HTTP_CODE2=$(docker exec $CONTAINER_ID curl -s -o /dev/null -w "%{http_code}" "$DOWNLOAD_URL2" 2>/dev/null)
    if [ "$HTTP_CODE2" = "200" ]; then
        echo "   ✅ 使用宿主机 IP 可以访问下载接口"
        echo "   建议：修改配置使用宿主机 IP"
    fi
fi

echo ""
echo "=========================================="
echo "测试完成"
echo "=========================================="
echo ""
echo "💡 如果 host.docker.internal 无法访问，可以："
echo "   1. 修改 application-dev.yml 中的 only-office-base-url"
echo "   2. 使用宿主机 IP: http://192.168.100.101:8080"
echo "   3. 确保后端服务监听 0.0.0.0:8080（不是 127.0.0.1:8080）"
echo ""


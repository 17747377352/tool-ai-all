# 快速修复下载失败（错误 -4）

## 问题

OnlyOffice 容器无法访问后端服务下载文件。

## 立即执行的修复步骤

### 1. 修改配置（已自动完成）

配置已更新为使用宿主机 IP：`http://192.168.100.101:8080`

### 2. 重启后端服务

```bash
# 停止当前服务（Ctrl+C）
cd tool-ai-service
mvn spring-boot:run
```

### 3. 验证容器访问

在服务器上运行：

```bash
# 测试容器能否访问后端
docker exec onlyoffice-ds curl -I "http://192.168.100.101:8080/health"
```

如果返回 `HTTP/1.1 200 OK`，说明可以访问。

### 4. 如果仍然无法访问

**选项 A：添加 extra_hosts（推荐）**

重新启动 OnlyOffice 容器，添加网络配置：

```bash
docker stop onlyoffice-ds
docker rm onlyoffice-ds

docker run -d -p 29000:80 \
  --add-host=host.docker.internal:host-gateway \
  --name onlyoffice-ds \
  -e JWT_ENABLED=false \
  onlyoffice/documentserver:7.5
```

**选项 B：检查后端服务监听地址**

确保 `application.yml` 中有：

```yaml
server:
  port: 8080
  address: 0.0.0.0  # 必须监听所有网络接口
```

### 5. 运行完整测试

```bash
./test-container-access.sh
```

## 常见原因

1. **后端服务只监听 localhost**：必须监听 `0.0.0.0`
2. **容器网络隔离**：macOS/Windows 上容器无法直接访问宿主机
3. **防火墙阻止**：检查防火墙设置

## 验证清单

- [ ] 后端服务监听 `0.0.0.0:8080`
- [ ] 配置使用宿主机 IP（192.168.100.101:8080）
- [ ] 从容器内可以访问后端服务
- [ ] 后端服务已重启


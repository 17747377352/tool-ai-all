# 修复 OnlyOffice 下载失败（错误 -4）

## 问题原因

错误 -4 "Download failed" 表示 OnlyOffice Document Server 无法从后端服务下载文件。

最常见的原因是：**OnlyOffice 容器无法访问后端服务**（网络隔离问题）。

## 快速修复方案（推荐）

### 方案 1：使用 Host 网络模式（最简单）

重新启动 OnlyOffice 容器，使用 host 网络模式：

```bash
# 停止并删除旧容器
docker stop onlyoffice-ds
docker rm onlyoffice-ds

# 使用 host 网络模式启动（容器直接使用宿主机网络）
docker run -d --network host --name onlyoffice-ds \
  -e JWT_ENABLED=false \
  onlyoffice/documentserver:7.5
```

**优点**：容器可以直接访问宿主机上的服务，无需配置网络

**缺点**：端口直接暴露在宿主机上（但你已经配置了安全组）

### 方案 2：检查后端服务监听地址

确保后端服务监听 `0.0.0.0` 而不是 `127.0.0.1`：

检查 `application.yml`：
```yaml
server:
  port: 8080
  address: 0.0.0.0  # 必须监听所有网络接口
```

然后重启后端服务。

### 方案 3：使用 Docker 网络（如果两个服务都在容器中）

如果后端也在容器中，可以创建共享网络：

```bash
# 创建网络
docker network create onlyoffice-network

# 启动后端容器（假设后端也在容器中）
docker run -d --network onlyoffice-network --name backend ...

# 启动 OnlyOffice 容器
docker run -d --network onlyoffice-network --name onlyoffice-ds \
  -e JWT_ENABLED=false \
  onlyoffice/documentserver:7.5
```

## 验证步骤

### 1. 运行调试脚本

在服务器上运行：

```bash
./debug-onlyoffice.sh
```

### 2. 手动测试容器访问

```bash
# 从 OnlyOffice 容器内测试访问后端
docker exec onlyoffice-ds curl -v "http://192.168.100.101:8080/onlyoffice/download?fn=demo.docx"
```

如果返回 `HTTP/1.1 200 OK`，说明可以访问。

### 3. 检查后端日志

```bash
tail -f tool-ai-service/logs/tool.log | grep -i "onlyoffice\|download"
```

如果看到 "OnlyOffice下载请求" 日志，说明请求到达了后端。

## 完整修复流程

```bash
# 1. 停止旧容器
docker stop onlyoffice-ds
docker rm onlyoffice-ds

# 2. 使用 host 网络模式启动新容器
docker run -d --network host --name onlyoffice-ds \
  -e JWT_ENABLED=false \
  onlyoffice/documentserver:7.5

# 3. 等待 2-3 分钟让服务启动

# 4. 验证服务
curl -I http://192.168.100.101:29000/welcome

# 5. 测试容器访问后端
docker exec onlyoffice-ds curl -I "http://192.168.100.101:8080/onlyoffice/download?fn=demo.docx"

# 6. 刷新浏览器页面
```

## 如果仍然失败

### 检查清单

- [ ] 后端服务已重启（监听 0.0.0.0:8080）
- [ ] OnlyOffice 容器使用 host 网络模式
- [ ] 从容器内可以访问后端服务
- [ ] 后端日志显示下载请求
- [ ] 文件存储目录存在且有权限

### 查看详细日志

```bash
# 后端日志
tail -f tool-ai-service/logs/tool.log

# OnlyOffice 容器日志
docker logs -f onlyoffice-ds
```

## 常见问题

### Q: 为什么容器无法访问后端？

A: Docker 容器默认使用桥接网络，与宿主机网络隔离。使用 `--network host` 可以让容器直接使用宿主机网络。

### Q: Host 网络模式安全吗？

A: 对于内网部署，host 网络模式是安全的。你已经配置了安全组限制外部访问。

### Q: 还有其他方法吗？

A: 可以配置 Docker 网络或使用 Nginx 反向代理，但 host 网络模式最简单。


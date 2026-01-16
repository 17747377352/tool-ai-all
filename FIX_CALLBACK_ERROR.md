# 修复 OnlyOffice 保存失败问题

## 问题描述

错误提示："The document could not be saved. Please check connection settings or contact your administrator."

这个错误表示 OnlyOffice Document Server 无法访问回调 URL 来保存文档。

## 已修复的问题

1. ✅ **添加了 CORS 响应头**：允许 OnlyOffice 跨域访问回调接口
2. ✅ **改进了响应格式**：使用 `response.getWriter()` 确保正确的 JSON 响应
3. ✅ **添加了详细日志**：便于排查问题

## 排查步骤

### 1. 检查后端日志

查看是否有回调请求：

```bash
tail -f tool-ai-service/logs/tool.log | grep -i "callback\|OnlyOffice回调"
```

如果**没有看到回调日志**，说明 OnlyOffice 无法访问回调 URL。

### 2. 测试回调接口

在服务器上测试回调接口是否可访问：

```bash
# 测试回调接口（模拟 OnlyOffice 的请求）
curl -X POST "http://192.168.100.101:8080/onlyoffice/callback?fn=demo.docx" \
  -H "Content-Type: application/json" \
  -d '{"status":2,"url":"http://example.com/file.docx"}'
```

应该返回：`{"error":0}`

### 3. 从 OnlyOffice 容器内测试

```bash
# 从容器内测试回调接口
docker exec onlyoffice-ds curl -X POST \
  "http://192.168.100.101:8080/onlyoffice/callback?fn=demo.docx" \
  -H "Content-Type: application/json" \
  -d '{"status":2,"url":"http://example.com/file.docx"}'
```

如果连接失败，说明网络不通。

## 解决方案

### 方案 1：使用 Host 网络模式（推荐）

如果 OnlyOffice 容器无法访问后端服务，使用 host 网络模式：

```bash
# 停止并删除旧容器
docker stop onlyoffice-ds
docker rm onlyoffice-ds

# 使用 host 网络模式启动
docker run -d --network host --name onlyoffice-ds \
  -e JWT_ENABLED=false \
  onlyoffice/documentserver:7.5
```

### 方案 2：检查 base-url 配置

确保 `application-dev.yml` 中的 `base-url` 配置正确：

```yaml
local:
  file:
    base-url: http://192.168.100.101:8080  # 必须是 OnlyOffice 容器能访问的地址
```

**重要**：这个地址必须是 OnlyOffice 容器能够访问到的，不能是 `localhost` 或 `127.0.0.1`。

### 方案 3：检查后端服务监听地址

确保后端服务监听 `0.0.0.0:8080`（不是 `127.0.0.1:8080`）：

```yaml
server:
  port: 8080
  address: 0.0.0.0  # 监听所有网络接口
```

## 验证修复

修复后，测试保存功能：

1. **打开编辑器**
2. **编辑文档内容**
3. **等待自动保存**（或手动保存）
4. **查看后端日志**，应该看到：
   ```
   OnlyOffice回调: fileName=demo.docx, body={...}
   开始保存文件: fileName=demo.docx, url=...
   文件保存成功: fileName=demo.docx, size=...
   ```

## 常见问题

### Q: 为什么保存失败？

A: 最常见的原因是 OnlyOffice 容器无法访问后端服务的回调 URL。使用 host 网络模式可以解决。

### Q: 如何确认回调被调用？

A: 查看后端日志，如果看到 "OnlyOffice回调" 日志，说明回调被调用了。

### Q: 回调返回什么格式？

A: OnlyOffice 要求返回 JSON 格式：`{"error":0}` 表示成功，`{"error":1,"message":"..."}` 表示失败。

## 完整检查清单

- [ ] 后端服务监听 `0.0.0.0:8080`
- [ ] `base-url` 配置为 OnlyOffice 容器可访问的地址
- [ ] OnlyOffice 容器使用 host 网络模式（或网络配置正确）
- [ ] 从容器内可以访问回调接口
- [ ] 回调接口返回正确的 JSON 格式
- [ ] 后端日志显示回调请求


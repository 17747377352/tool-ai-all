# macOS Docker 网络问题修复指南

## 问题

错误 -4 "Download failed" 表示 OnlyOffice 容器无法访问后端服务。

## 快速修复方案

### 方案 1：使用宿主机 IP（推荐，最可靠）

如果 `host.docker.internal` 不工作，直接使用宿主机 IP：

**修改 `application-dev.yml`**：

```yaml
local:
  file:
    only-office-base-url: http://192.168.100.101:8080  # 使用宿主机 IP
```

**前提条件**：
- 后端服务监听 `0.0.0.0:8080`（已在 application.yml 中配置）
- 宿主机 IP 是 `192.168.100.101`（固定 IP）

### 方案 2：启用 host.docker.internal（如果方案1不行）

某些 Docker 版本需要手动启用 `host.docker.internal`：

1. **Docker Desktop 设置**：
   - 打开 Docker Desktop
   - Settings → Resources → Network
   - 确保启用 "Allow containers to access host network"

2. **或者添加 extra_hosts**：

```bash
docker stop onlyoffice-ds
docker rm onlyoffice-ds

docker run -d -p 29000:80 \
  --add-host=host.docker.internal:host-gateway \
  --name onlyoffice-ds \
  -e JWT_ENABLED=false \
  onlyoffice/documentserver:7.5
```

## 验证步骤

### 1. 运行测试脚本

```bash
./test-container-access.sh
```

脚本会测试：
- host.docker.internal 是否可解析
- 容器能否访问后端服务
- 下载接口是否可访问

### 2. 手动测试

```bash
# 从容器内测试访问后端
docker exec onlyoffice-ds curl -v "http://192.168.100.101:8080/health"

# 或测试 host.docker.internal
docker exec onlyoffice-ds curl -v "http://host.docker.internal:8080/health"
```

### 3. 检查后端服务监听地址

确保后端服务监听所有网络接口：

```yaml
# application.yml
server:
  port: 8080
  address: 0.0.0.0  # 必须监听所有接口
```

## 推荐配置（macOS）

**application-dev.yml**：

```yaml
local:
  file:
    base-url: http://192.168.100.101:8080  # 浏览器访问
    only-office-base-url: http://192.168.100.101:8080  # 容器访问（使用宿主机 IP）
```

**为什么使用宿主机 IP？**
- 更可靠，不依赖 Docker 的特殊配置
- 在 macOS/Windows/Linux 上都可用
- 只要宿主机 IP 固定即可

## 完整修复流程

```bash
# 1. 修改配置（使用宿主机 IP）
# 编辑 application-dev.yml，设置 only-office-base-url: http://192.168.100.101:8080

# 2. 重启后端服务
cd tool-ai-service
mvn spring-boot:run

# 3. 测试容器访问
docker exec onlyoffice-ds curl -I "http://192.168.100.101:8080/health"

# 4. 如果测试成功，刷新浏览器页面
```

## 如果仍然失败

检查：
1. 后端服务是否运行在 8080 端口
2. 后端服务是否监听 0.0.0.0（不是 127.0.0.1）
3. 防火墙是否阻止了访问
4. 宿主机 IP 是否正确


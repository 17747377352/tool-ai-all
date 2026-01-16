# OnlyOffice Docker 网络配置（macOS/Windows）

## 问题

macOS 和 Windows 不支持 Docker 的 `--network host` 模式，需要其他方式让 OnlyOffice 容器访问后端服务。

## 解决方案

### 方案 1：使用 host.docker.internal（推荐，最简单）

在 macOS 和 Windows 上，Docker 提供了 `host.docker.internal` 来访问宿主机服务。

#### 步骤 1：修改后端配置

编辑 `application-dev.yml`，将 `base-url` 改为使用 `host.docker.internal`：

```yaml
local:
  file:
    base-url: http://host.docker.internal:8080
```

**注意**：这个地址只在 OnlyOffice 容器内部使用，前端仍然使用 `192.168.100.101:8080`。

#### 步骤 2：修改 OnlyOfficeController

需要根据请求来源动态生成 URL：
- 如果请求来自 OnlyOffice 容器，使用 `host.docker.internal:8080`
- 如果请求来自浏览器，使用 `192.168.100.101:8080`

或者更简单的方法：在配置中同时提供两个地址。

### 方案 2：使用 Docker 网络 + 端口映射（推荐）

创建 Docker 网络，让容器可以通过服务名访问。

#### 步骤 1：创建 Docker 网络

```bash
docker network create onlyoffice-net
```

#### 步骤 2：启动 OnlyOffice 容器（使用网络）

```bash
docker stop onlyoffice-ds
docker rm onlyoffice-ds

docker run -d \
  --network onlyoffice-net \
  -p 29000:80 \
  --name onlyoffice-ds \
  -e JWT_ENABLED=false \
  onlyoffice/documentserver:7.5
```

#### 步骤 3：配置后端服务地址

由于后端服务在宿主机上，OnlyOffice 容器需要使用特殊方式访问：

**选项 A**：使用 `host.docker.internal`（macOS/Windows）
- 在 OnlyOffice 容器内，使用 `http://host.docker.internal:8080` 访问后端

**选项 B**：使用宿主机 IP
- 使用 `http://192.168.100.101:8080`（如果宿主机 IP 固定）

### 方案 3：修改后端配置支持动态 URL（最佳方案）

让后端根据请求来源返回不同的 URL。


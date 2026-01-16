# OnlyOffice 协同编辑集成说明

## 一、环境准备

### 1. 启动 OnlyOffice Document Server

```bash
docker pull onlyoffice/documentserver:7.5
docker run -d -p 9000:80 onlyoffice/documentserver:7.5
```

访问 http://localhost:9000/welcome 确认服务正常运行。

### 2. 配置后端服务地址

编辑 `tool-ai-service/src/main/resources/application-dev.yml`，确保 `local.file.base-url` 配置正确：

```yaml
local:
  file:
    base-path: /Users/gongxuesong/Work/test/tool
    base-url: http://localhost:8080  # 修改为实际的后端服务地址
```

**重要**：如果 OnlyOffice Document Server 运行在 Docker 容器中，需要确保：
- `base-url` 使用宿主机 IP（不能使用 localhost），例如：`http://192.168.1.100:8080`
- OnlyOffice Document Server 能够访问到 `base-url` 的 `/onlyoffice/download` 和 `/onlyoffice/callback` 接口

### 3. 配置前端 OnlyOffice Document Server 地址

编辑 `tool-ai-uniapp/pages/onlyoffice/onlyoffice.vue`，修改 `documentServerUrl`：

```javascript
documentServerUrl: 'http://localhost:9000'  // 修改为实际的 OnlyOffice Document Server 地址
```

**重要**：如果 OnlyOffice Document Server 运行在 Docker 容器中，需要使用宿主机 IP。

## 二、使用方式

### 1. 启动后端服务

```bash
cd tool-ai-service
mvn spring-boot:run
```

### 2. 启动前端（uniapp）

在 HBuilderX 中打开项目，运行到微信小程序或 H5。

### 3. 访问编辑页面

在 uniapp 中跳转到编辑页面：

```javascript
uni.navigateTo({
    url: '/pages/onlyoffice/onlyoffice?fileName=demo.docx'
});
```

或者直接访问 HTML 页面（H5 环境）：

```
http://localhost:8080/onlyoffice-editor.html?fileName=demo.docx&documentServerUrl=http://localhost:9000
```

## 三、接口说明

### 1. 获取编辑器配置

**接口**：`GET /onlyoffice/config?fileName=demo.docx`

**返回**：
```json
{
    "documentType": "text",
    "document": {
        "title": "demo.docx",
        "url": "http://localhost:8080/onlyoffice/download?fn=demo.docx",
        "fileType": "docx"
    },
    "editorConfig": {
        "callbackUrl": "http://localhost:8080/onlyoffice/callback?fn=demo.docx",
        "mode": "edit",
        "user": {
            "id": "1",
            "name": "用户1"
        }
    }
}
```

### 2. 文件下载接口

**接口**：`GET /onlyoffice/download?fn=demo.docx`

OnlyOffice Document Server 会调用此接口下载文件。

### 3. 文件保存回调接口

**接口**：`POST /onlyoffice/callback?fn=demo.docx`

OnlyOffice Document Server 会调用此接口保存文件。

## 四、文件存储

文件存储在 `local.file.base-path/onlyoffice/` 目录下。

例如：`/Users/gongxuesong/Work/test/tool/onlyoffice/demo.docx`

## 五、测试多人协同编辑

1. 打开两个浏览器窗口（或两个设备）
2. 访问同一个文件的编辑页面
3. 输入不同的用户名（可以通过修改后端代码或添加用户选择功能）
4. 在一个窗口中编辑，另一个窗口会实时看到光标和编辑内容

## 六、常见问题

### 1. 编辑器无法加载

- 检查 OnlyOffice Document Server 是否正常运行（访问 http://localhost:9000/welcome）
- 检查浏览器控制台是否有错误
- 确认 `documentServerUrl` 配置正确

### 2. 文件无法保存

- 检查后端日志，查看 callback 接口是否被调用
- 确认 `base-url` 配置正确，OnlyOffice Document Server 能够访问
- 检查文件存储目录权限

### 3. 多人协同不生效

- 确认两个窗口访问的是同一个文件
- 检查用户 ID 是否不同（相同用户 ID 不会显示多个光标）
- 确认网络连接正常

## 七、生产环境部署注意事项

1. **OnlyOffice Document Server 地址**：使用公网可访问的地址
2. **后端服务地址**：使用公网可访问的地址，确保 OnlyOffice Document Server 能够访问
3. **文件存储路径**：使用绝对路径，确保有读写权限
4. **安全配置**：生产环境建议添加认证和授权机制
5. **HTTPS**：OnlyOffice Document Server 要求使用 HTTPS（生产环境）

## 八、支持的文件类型

- **Word**: .docx, .doc
- **Excel**: .xlsx, .xls
- **PowerPoint**: .pptx, .ppt


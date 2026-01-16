# OnlyOffice 协同编辑测试指南

## 一、测试前准备

### 1. 检查配置

#### 后端配置检查

编辑 `tool-ai-service/src/main/resources/application-dev.yml`：

```yaml
local:
  file:
    base-path: /Users/gongxuesong/Work/test/tool
    base-url: http://123.56.22.101:8080  # ⚠️ 重要：这里应该是后端服务地址，不是 OnlyOffice 地址
```

**注意**：`base-url` 必须是 OnlyOffice Document Server 能够访问到的后端服务地址。

#### 前端配置检查

1. **API 配置** (`tool-ai-uniapp/common/config/api-config.js`)：
   ```javascript
   const BASE_URL = 'http://123.56.22.101:8080';  // 后端服务地址
   ```

2. **OnlyOffice Document Server 地址** (`tool-ai-uniapp/pages/onlyoffice/onlyoffice.vue`)：
   ```javascript
   documentServerUrl: 'http://123.56.22.101:29000'  // OnlyOffice Document Server 地址
   ```

### 2. 确保服务运行

#### 步骤 1：启动 OnlyOffice Document Server

```bash
# 如果还没有运行，启动容器
docker run -d -p 29000:80 --name onlyoffice-ds onlyoffice/documentserver:7.5

# 或者如果已经运行，检查状态
docker ps | grep onlyoffice

# 访问测试页面，应该能看到欢迎页
curl http://123.56.22.101:29000/welcome
```

#### 步骤 2：启动后端服务

```bash
cd tool-ai-service
mvn spring-boot:run
```

**验证后端接口**：
```bash
# 测试配置接口
curl "http://123.56.22.101:8080/onlyoffice/config?fileName=demo.docx"

# 应该返回 JSON 配置
```

#### 步骤 3：启动前端（H5 模式）

**方式一：使用 HBuilderX（推荐）**
1. 打开 HBuilderX
2. 文件 → 打开目录 → 选择 `tool-ai-uniapp`
3. 运行 → 运行到浏览器 → Chrome

**方式二：使用命令行**
```bash
cd tool-ai-uniapp
npm run dev:h5
```

## 二、测试步骤

### 测试 1：单用户编辑测试

1. **访问编辑页面**
   - 在浏览器中打开：`http://localhost:8080/onlyoffice-editor.html?fileName=demo.docx&documentServerUrl=http://123.56.22.101:29000`
   - 或者在 uniapp 中跳转：
     ```javascript
     uni.navigateTo({
         url: '/pages/onlyoffice/onlyoffice?fileName=demo.docx'
     });
     ```

2. **检查编辑器加载**
   - 应该能看到 OnlyOffice 编辑器界面
   - 可以输入文字、编辑内容

3. **检查文件保存**
   - 编辑内容后，等待几秒（OnlyOffice 会自动保存）
   - 检查后端日志，应该看到 callback 调用
   - 检查文件是否保存：`ls -lh /Users/gongxuesong/Work/test/tool/onlyoffice/demo.docx`

### 测试 2：多人协同编辑测试

1. **打开第一个浏览器窗口**
   - 访问编辑页面（同上）
   - 在浏览器控制台查看用户信息（F12 → Console）
   - 应该看到类似：`OnlyOffice配置生成: fileName=demo.docx, userId=1, userName=用户1`

2. **打开第二个浏览器窗口（或不同设备）**
   - 访问同一个文件的编辑页面
   - **重要**：需要修改用户信息（见下方"修改用户信息"部分）

3. **测试协同编辑**
   - 在第一个窗口输入文字
   - 第二个窗口应该能看到实时更新
   - 两个窗口应该能看到不同的光标（不同用户）

### 修改用户信息（用于多人测试）

编辑 `OnlyOfficeController.java` 的 `config` 方法，临时修改用户信息：

```java
// 临时测试：从请求参数获取用户信息
String userId = request.getParameter("userId");
String userName = request.getParameter("userName");
if (userId == null) {
    userId = "1";
}
if (userName == null) {
    userName = "用户" + userId;
}
```

然后访问时带上参数：
```
http://localhost:8080/onlyoffice-editor.html?fileName=demo.docx&userId=1&userName=张三&documentServerUrl=http://123.56.22.101:29000
http://localhost:8080/onlyoffice-editor.html?fileName=demo.docx&userId=2&userName=李四&documentServerUrl=http://123.56.22.101:29000
```

## 三、测试检查清单

### ✅ 基础功能检查

- [ ] OnlyOffice Document Server 正常运行（访问 `/welcome` 能看到欢迎页）
- [ ] 后端服务正常运行（访问 `/health` 返回正常）
- [ ] 配置接口返回正确 JSON（`/onlyoffice/config`）
- [ ] 编辑器页面能正常加载
- [ ] 可以编辑文档内容
- [ ] 文件能正常保存（检查 callback 日志和文件）

### ✅ 多人协同检查

- [ ] 两个窗口打开同一个文件
- [ ] 两个窗口显示不同的用户信息
- [ ] 在一个窗口编辑，另一个窗口能看到实时更新
- [ ] 两个窗口能看到不同的光标（不同颜色）

### ✅ 文件类型测试

- [ ] Word 文档（.docx）
- [ ] Excel 表格（.xlsx）
- [ ] PowerPoint 演示文稿（.pptx）

## 四、常见问题排查

### 问题 1：编辑器无法加载

**症状**：页面显示"加载失败"或空白

**排查步骤**：
1. 打开浏览器控制台（F12），查看错误信息
2. 检查 OnlyOffice Document Server 是否运行：
   ```bash
   curl http://123.56.22.101:29000/welcome
   ```
3. 检查网络请求：
   - 查看 Network 标签，确认 API 脚本是否加载成功
   - 确认配置接口是否返回正确数据

**解决方案**：
- 确保 OnlyOffice Document Server 正常运行
- 检查 `documentServerUrl` 配置是否正确
- 检查浏览器控制台的错误信息

### 问题 2：文件无法保存

**症状**：编辑后文件没有保存

**排查步骤**：
1. 查看后端日志：
   ```bash
   tail -f tool-ai-service/logs/tool.log | grep callback
   ```
2. 检查 callback 接口是否被调用
3. 检查文件存储目录权限：
   ```bash
   ls -ld /Users/gongxuesong/Work/test/tool/onlyoffice
   ```

**解决方案**：
- 确保 `base-url` 配置正确，OnlyOffice Document Server 能访问到
- 确保文件存储目录有写权限
- 检查后端日志中的错误信息

### 问题 3：多人协同不生效

**症状**：两个窗口看不到对方的编辑

**排查步骤**：
1. 确认两个窗口访问的是同一个文件
2. 确认用户 ID 不同（查看后端日志）
3. 检查网络连接

**解决方案**：
- 确保两个窗口使用不同的用户 ID
- 确保网络连接正常
- 检查 OnlyOffice Document Server 日志

### 问题 4：CORS 跨域问题

**症状**：浏览器控制台显示 CORS 错误

**解决方案**：
- 后端已经添加了 `@CrossOrigin` 注解
- 如果还有问题，检查 OnlyOffice Document Server 的配置

## 五、快速测试命令

```bash
# 1. 检查 OnlyOffice Document Server
curl http://123.56.22.101:29000/welcome

# 2. 检查后端配置接口
curl "http://123.56.22.101:8080/onlyoffice/config?fileName=demo.docx"

# 3. 检查文件是否存在
ls -lh /Users/gongxuesong/Work/test/tool/onlyoffice/

# 4. 查看后端日志
tail -f tool-ai-service/logs/tool.log | grep -i onlyoffice

# 5. 测试文件下载接口
curl "http://123.56.22.101:8080/onlyoffice/download?fn=demo.docx" -o test.docx
```

## 六、测试 URL 模板

### H5 直接访问（推荐用于快速测试）

```
http://你的后端地址:8080/onlyoffice-editor.html?fileName=demo.docx&documentServerUrl=http://123.56.22.101:29000
```

### 带用户参数的 URL（用于多人测试）

```
# 用户1
http://你的后端地址:8080/onlyoffice-editor.html?fileName=demo.docx&userId=1&userName=张三&documentServerUrl=http://123.56.22.101:29000

# 用户2
http://你的后端地址:8080/onlyoffice-editor.html?fileName=demo.docx&userId=2&userName=李四&documentServerUrl=http://123.56.22.101:29000
```

## 七、预期结果

### 成功标志

1. ✅ 编辑器正常加载，显示文档内容
2. ✅ 可以正常编辑（输入文字、修改格式等）
3. ✅ 编辑后自动保存（查看后端日志确认）
4. ✅ 文件保存在指定目录
5. ✅ 多人编辑时能看到实时更新和不同光标

### 测试通过标准

- 单用户编辑：✅ 可以编辑并保存
- 多人协同：✅ 两个用户同时编辑，能看到对方的实时更新
- 文件保存：✅ 编辑内容正确保存到服务器

---

**提示**：如果遇到问题，先检查浏览器控制台和后端日志，大部分问题都能从日志中找到原因。


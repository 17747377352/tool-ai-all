# 调试 JWT 拦截器 "未授权" 错误

## 错误信息

```
com.example.tool.exception.BusinessException: 未授权
at com.example.tool.config.JwtInterceptor.preHandle(JwtInterceptor.java:32)
```

## 原因分析

JWT拦截器在第32行抛出"未授权"异常，说明请求没有携带token。

可能的原因：
1. **请求未携带 Authorization Header**
   - 前端请求没有设置 `Authorization: Bearer {token}`
   - 或者 token 为空

2. **访问了需要认证的接口但没有登录**
   - 用户未登录或token已过期
   - 前端没有正确保存token

3. **访问了根路径或其他不需要认证的路径**
   - 浏览器访问 `http://localhost:8080/` 会触发拦截器
   - 这些路径应该加入白名单

## 已修复的问题

### 1. 添加了详细日志

JWT拦截器现在会记录：
- 请求方法和路径
- 是否携带token
- token验证结果

### 2. 扩展了白名单

添加了以下路径到白名单：
- `/` - 根路径
- `/health` - 健康检查
- `/favicon.ico` - 浏览器图标请求
- `/actuator/**` - Spring Boot Actuator（如果使用）

### 3. 添加了健康检查接口

创建了 `HealthController`，提供：
- `GET /` - 根路径健康检查
- `GET /health` - 健康检查接口

## 调试步骤

### 1. 查看后端日志

现在JWT拦截器会输出详细日志：
```
拦截请求: GET /
请求未携带token: GET /
```

### 2. 检查前端请求

在微信开发者工具的 Network 面板中：
1. 查看需要认证的请求
2. 检查 Request Headers：
   ```
   Authorization: Bearer {token}
   ```

### 3. 检查 Token 是否保存

在控制台执行：
```javascript
console.log('Token:', uni.getStorageSync('token'))
```

### 4. 验证登录流程

1. 打开小程序
2. 查看控制台，应该看到：
   - "登录成功，token已保存"
3. 查看后端日志，应该看到：
   - "拦截请求: POST /auth/wx-login"（白名单，不拦截）
   - "拦截请求: POST /tool/xxx"（需要token）

## 常见场景

### 场景1：浏览器访问根路径

**问题**：直接在浏览器访问 `http://localhost:8080/`

**解决**：已添加到白名单，现在不会报错，会返回健康检查信息

### 场景2：前端请求未携带token

**问题**：前端某些请求没有使用封装的 `request.js`

**解决**：
1. 确保所有需要认证的请求都使用 `api.js` 中的方法
2. 检查 `request.js` 是否正确添加了 Authorization header

### 场景3：Token 未保存或已过期

**问题**：登录失败或token已过期

**解决**：
1. 检查登录是否成功
2. 检查token是否保存到本地存储
3. 如果过期，重新登录

## 验证修复

### 测试1：访问根路径

在浏览器访问：`http://localhost:8080/`

**预期结果**：
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "status": "ok",
        "service": "tool-ai-service"
    }
}
```

### 测试2：访问需要认证的接口（不带token）

**预期结果**：
- 返回 401 错误
- 后端日志显示："请求未携带token"

### 测试3：访问需要认证的接口（带token）

**预期结果**：
- 正常返回数据
- 后端日志显示请求信息

## 日志示例

### 正常请求日志
```
拦截请求: POST /tool/name-sign
```

### 未授权请求日志
```
拦截请求: GET /
请求未携带token: GET /
```

### Token无效日志
```
拦截请求: POST /tool/name-sign
Token验证失败: ...
```

## 如果问题仍然存在

1. **查看完整日志**
   - 确认是哪个接口报错
   - 确认请求方法和路径

2. **检查前端代码**
   - 确认是否使用了 `api.js`
   - 确认token是否正确传递

3. **检查网络请求**
   - 在Network面板查看请求详情
   - 确认Header中是否有Authorization

4. **清除缓存重新登录**
   ```javascript
   uni.clearStorageSync()
   // 重新打开小程序
   ```





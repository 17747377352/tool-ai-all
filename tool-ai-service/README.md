# Tool AI Service

AI工具箱后端服务

## 技术栈

- Spring Boot 2.4.5
- MyBatis Plus 3.5.3.1
- MySQL 8.0
- Redis
- JWT认证

## 功能模块

### 1. 认证模块
- 微信小程序登录（code2session）
- 用户信息解密
- JWT Token生成和验证

### 2. 工具模块
- 短视频去水印
- AI头像生成
- 姓氏签名生成
- 运势测试

### 3. 限流模块
- 每日限流：去水印30次，其他各10次
- 超限返回429状态码

## 配置说明

1. 修改 `application-dev.yml` 中的数据库和Redis配置
2. 修改 `application-dev.yml` 中的微信小程序配置（appid、secret）
3. 修改 `application-dev.yml` 中的DeepSeek API配置（如需要）

## 数据库初始化

执行 `src/main/resources/sql/init.sql` 中的SQL语句初始化数据库。

## API接口

### 认证接口
- `POST /auth/wx-login` - 微信登录
- `POST /auth/decrypt-userinfo` - 解密用户信息（需要Token）

### 工具接口（需要Token）
- `POST /tool/remove-logo` - 去水印
- `POST /tool/ai-avatar` - AI头像生成
- `POST /tool/name-sign` - 姓氏签名生成
- `POST /tool/fortune` - 运势测试

## 统一返回格式

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {}
}
```

## 异常处理

- 401: 未授权/Token无效/Token过期
- 429: 请求过于频繁（限流）
- 500: 系统错误

## 注意事项

1. JWT Token有效期30天
2. 每日限流规则：去水印30次，其他各10次
3. 所有工具接口都需要在Header中携带Token：`Authorization: Bearer {token}`
4. 白名单接口：`/auth/wx-login`、`/error`、`/swagger-ui/**`、`/v3/api-docs/**`





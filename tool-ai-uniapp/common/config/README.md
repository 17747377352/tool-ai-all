# 配置文件说明

本目录存放项目的统一配置文件。

## 文件说明

### api-config.js
后端服务地址统一配置文件。

**使用方式：**
```javascript
import apiConfig from '@/common/config/api-config.js';

// 使用后端地址
const url = `${apiConfig.BASE_URL}/auth/wx-login`;
```

**配置说明：**
- 开发环境：默认使用 `http://localhost:8080`
- 生产环境：修改 `BASE_URL` 为实际的后端服务地址

**修改步骤：**
1. 打开 `api-config.js` 文件
2. 注释掉开发环境的 `BASE_URL`
3. 取消注释生产环境的 `BASE_URL` 并修改为实际地址
4. 保存文件

**示例：**
```javascript
// 开发环境（注释掉）
// const BASE_URL = 'http://localhost:8080';

// 生产环境（取消注释并修改）
const BASE_URL = 'https://api.yourdomain.com';
```

### ad-config.js
广告配置统一管理文件。

## 注意事项

- 所有后端地址的引用都应该从 `api-config.js` 导入
- 不要在代码中硬编码后端地址
- 修改配置后需要重新编译项目

/**
 * API配置
 * 统一管理后端服务地址
 * 
 * 使用说明：
 * 1. 开发环境：使用 http://localhost:8080
 * 2. 生产环境：修改为实际的后端服务地址
 * 3. 所有前端代码统一从此文件导入 baseUrl
 */

// 后端服务基础地址
// 开发环境
const BASE_URL = 'http://localhost:8080';

// 生产环境（部署时取消注释并修改）
// const BASE_URL = 'http://123.56.22.101:38080';

export default {
    BASE_URL
};


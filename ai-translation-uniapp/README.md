# AI翻译工具小程序

基于uni-app开发的AI翻译工具小程序，对接`ai-translation-service`后端服务。

## 功能列表

1. **即时翻译** - 支持中译英、中译日、中译蒙、英译中、日译中、蒙译中
2. **老照片修复** - 单张/批量修复老照片（GFPGAN）
3. **图生图** - 基于参考图片生成新图片
4. **生成图片** - 字生图和模版生图（支持模版同款和模版参考图）
5. **AI识图+翻译** - 识别图片中的文字并翻译（后端待开发）
6. **蒙古语AI对话** - 蒙古语AI对话功能（后端待开发，前端已预留入口）

## 项目结构

```
ai-translation-uniapp/
├── common/
│   ├── components/          # 公共组件
│   │   └── ad-video-banner.vue
│   ├── config/              # 配置文件
│   │   ├── api-config.js    # API配置（后端地址）
│   │   └── ad-config.js     # 广告配置
│   └── utils/               # 工具类
│       ├── api.js           # API接口封装
│       ├── request.js       # 请求工具（含token刷新）
│       ├── auth.js         # 认证工具
│       └── ad.js            # 广告工具
├── pages/
│   ├── index/               # 首页（功能入口）
│   ├── translate/           # 即时翻译
│   ├── old-photo/           # 老照片修复
│   ├── image-to-image/      # 图生图
│   ├── image-generate/      # 生成图片
│   │   ├── image-generate.vue    # 字生图/模版生图
│   │   ├── template-list.vue     # 模版列表
│   │   └── task-list.vue         # 任务列表
│   ├── image-recognition/   # AI识图+翻译
│   ├── mongolian-chat/      # 蒙古语AI对话（入口）
│   ├── result/              # 结果展示
│   ├── user/                # 用户中心
│   └── feedback/            # 意见反馈
├── App.vue                  # 应用入口
├── main.js                  # 主入口文件
├── pages.json               # 页面配置
├── manifest.json            # 应用配置
└── package.json             # 依赖配置
```

## 配置说明

### API配置

编辑 `common/config/api-config.js`：

```javascript
// 开发环境
const BASE_URL = 'http://localhost:18090';

// 生产环境
// const BASE_URL = 'https://your-domain.com/api';
```

### 广告配置

编辑 `common/config/ad-config.js`：

```javascript
// 是否开启广告
export const ENABLE_AD = false; // 开发环境设为false

// 广告位ID
export const AD_CONFIG = {
    REWARDED_VIDEO_AD_UNIT_ID: 'adunit-xxx',
    INTERSTITIAL_AD_UNIT_ID: 'adunit-xxx',
    BANNER_AD_UNIT_ID: 'adunit-xxx'
};
```

## 开发说明

### 安装依赖

```bash
npm install
```

### 运行项目

```bash
# 微信小程序
npm run dev:mp-weixin

# H5
npm run dev:h5
```

### 构建项目

```bash
# 微信小程序
npm run build:mp-weixin

# H5
npm run build:h5
```

## 接口说明

### 认证接口
- `POST /auth/wx-login` - 微信登录
- `POST /auth/decrypt-userinfo` - 解密用户信息

### 工具接口
- `POST /tool/translate` - 即时翻译
- `POST /tool/restore-old-photo` - 老照片修复（单张）
- `POST /tool/batch-restore-old-photo` - 老照片修复（批量）
- `POST /tool/ai-avatar` - AI头像生成（字生图/图生图）
- `POST /tool/upload-image` - 图片上传
- `GET /tool/templates` - 获取图片模版列表
- `POST /tool/template-generate` - 使用模版生成图片（异步）
- `GET /tool/tasks` - 获取任务列表
- `GET /tool/task/{taskId}` - 获取任务详情
- `GET /tool/task/{taskId}/download` - 下载任务结果

## 注意事项

1. **广告功能**：当前版本已注释掉广告相关接口调用，因为`ai-translation-service`中暂无广告接口。如需启用，需要后端先实现相关接口。

2. **JWT拦截器**：当前后端JWT拦截器已临时关闭用于测试，前端会自动处理token刷新。

3. **图片上传**：所有图片上传都会先上传到OSS，返回公网可访问的URL。

4. **异步任务**：模版生图功能采用异步处理，任务创建后会返回taskId，前端需要轮询或定时刷新任务状态。

5. **AI识图+翻译**：前端已实现，但后端接口待开发，当前显示模拟数据。

6. **蒙古语AI对话**：前端已预留入口页面，后端接口待开发。

## 待开发功能

- [ ] AI识图+翻译后端接口
- [ ] 蒙古语AI对话后端接口
- [ ] 广告相关接口（如需要）

## 版本信息

- 版本：1.0.0
- 后端服务：ai-translation-service (端口18090)
- 框架：uni-app (Vue 3)



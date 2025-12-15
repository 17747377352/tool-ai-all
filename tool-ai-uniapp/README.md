# AI工具箱小程序

免费工具箱，用AI帮用户"去水印 → 做头像 → 做签名 → 测运势"

## 功能特性

- 短视频去水印
- AI头像生成
- 姓氏签名生成
- 运势测试
- 激励视频广告（保存前必须看完）
- 插屏广告（第1、3、5次保存触发）
- Banner广告（常驻）

## 技术栈

- uni-app
- Vue 3
- 微信小程序

## 配置说明

1. 修改 `manifest.json` 中的微信小程序 appid
2. 修改 `common/utils/request.js` 中的后端API地址
3. 修改 `common/utils/ad.js` 中的广告位ID
4. 修改 `common/components/ad-video-banner.vue` 中的广告位ID

## 开发

### 快速启动

**推荐使用 HBuilderX**（最简单）

1. 下载并安装 [HBuilderX](https://www.dcloud.io/hbuilderx.html)
2. 打开 HBuilderX，导入本项目
3. 配置 `manifest.json` 中的微信小程序 AppID
4. 配置 `common/utils/request.js` 中的后端API地址
5. 点击 `运行` → `运行到小程序模拟器` → `微信开发者工具`

**详细步骤请查看：[启动调试指南.md](./启动调试指南.md)**

## 注意事项

- 激励视频5秒内关闭视为无效
- 每日限流：去水印30次，其他各10次
- 需要配置微信小程序相关权限



# 图标文件说明

本目录存放首页工具卡片的图标文件。

## 当前状态

**注意**：目前代码中使用 emoji 作为占位符，项目可以正常编译和运行。如果需要使用真实的图标文件，请按照以下说明添加。

## 需要的图标文件（可选）

如果需要替换 emoji 占位符，可以添加以下图标文件：

- `remove-logo.png` - 去水印图标
- `ai-avatar.png` - AI头像图标
- `name-sign.png` - 姓氏签名图标
- `fortune.png` - 运势测试图标
- `constellation.png` - 星座运势图标
- `upload.png` - 上传图标（用于去水印页面）
- `default-avatar.png` - 默认头像（用于用户页面）

## 图标要求

- 尺寸：建议 200x200px 或更大（支持 @2x、@3x）
- 格式：PNG（支持透明背景）
- 风格：简洁、现代、符合工具功能特点

## 如何替换为真实图标

1. 将图标文件放到 `static/icons/` 目录下
2. 修改 `pages/index/index.vue`，将 emoji 占位符替换为：
   ```vue
   <image class="tool-icon" src="/static/icons/remove-logo.png" mode="aspectFit"></image>
   ```
3. 同样修改其他页面的图标引用

## 星座运势页面

星座选择页面使用emoji图标（♈♉♊等），不需要图片文件。


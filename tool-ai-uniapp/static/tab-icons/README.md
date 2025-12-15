# Tab 图标说明

## 需要的图标文件

请在此目录下放置以下图标文件：

1. **tool.png** - 工具箱图标（未选中状态）
   - 尺寸：81px × 81px
   - 格式：PNG
   - 颜色：灰色（#7A7E83）

2. **tool-active.png** - 工具箱图标（选中状态）
   - 尺寸：81px × 81px
   - 格式：PNG
   - 颜色：蓝色（#3c9cff）

3. **user.png** - 我的图标（未选中状态）
   - 尺寸：81px × 81px
   - 格式：PNG
   - 颜色：灰色（#7A7E83）

4. **user-active.png** - 我的图标（选中状态）
   - 尺寸：81px × 81px
   - 格式：PNG
   - 颜色：蓝色（#3c9cff）

## 图标要求

- **尺寸**：建议 81px × 81px（微信小程序推荐尺寸）
- **格式**：PNG（支持透明背景）
- **文件大小**：建议每个图标不超过 40KB

## 如何获取图标

1. **使用图标库**：
   - iconfont：https://www.iconfont.cn/
   - iconify：https://iconify.design/
   - 搜索关键词：tool、user、home、mine

2. **自己设计**：
   - 使用 Figma、Sketch 等设计工具
   - 导出为 PNG 格式

3. **临时方案**：
   - 可以使用简单的文字图标
   - 或者使用纯色方块作为占位符

## 配置 tabBar

准备好图标后，在 `pages.json` 中取消注释 tabBar 配置：

```json
"tabBar": {
    "color": "#7A7E83",
    "selectedColor": "#3c9cff",
    "borderStyle": "black",
    "backgroundColor": "#ffffff",
    "list": [
        {
            "pagePath": "pages/index/index",
            "iconPath": "static/tab-icons/tool.png",
            "selectedIconPath": "static/tab-icons/tool-active.png",
            "text": "工具箱"
        },
        {
            "pagePath": "pages/user/user",
            "iconPath": "static/tab-icons/user.png",
            "selectedIconPath": "static/tab-icons/user-active.png",
            "text": "我的"
        }
    ]
}
```

## 临时方案

如果暂时没有图标，可以：
1. 先不配置 tabBar（当前状态）
2. 通过页面导航进行页面切换
3. 准备好图标后再配置 tabBar





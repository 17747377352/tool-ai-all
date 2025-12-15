# 解决 "未找到 app.json" 错误

## 错误原因

直接用微信开发者工具打开 uni-app 源码目录时，会提示找不到 `app.json`。

**原因**：uni-app 项目需要先编译成微信小程序格式，才会生成 `app.json` 等文件。

## 解决方案

### ✅ 方案一：使用 HBuilderX 编译运行（推荐）

1. **打开 HBuilderX**
   - 下载安装：https://www.dcloud.io/hbuilderx.html

2. **导入项目**
   - `文件` → `导入` → `从本地目录导入`
   - 选择 `tool-ai-uniapp` 目录

3. **运行项目**
   - 点击顶部菜单：`运行` → `运行到小程序模拟器` → `微信开发者工具`
   - HBuilderX 会自动编译项目并生成 `app.json`
   - 编译完成后会自动打开微信开发者工具

4. **首次配置**
   - 如果提示配置微信开发者工具路径：
     - Windows: `C:\Program Files (x86)\Tencent\微信web开发者工具\cli.bat`
     - Mac: `/Applications/wechatwebdevtools.app/Contents/MacOS/cli`

### ✅ 方案二：手动编译后运行

如果你使用 CLI 方式：

```bash
cd tool-ai-uniapp

# 安装依赖（如果有 package.json）
npm install

# 编译到微信小程序
npm run dev:mp-weixin
# 或
npm run build:mp-weixin
```

编译完成后，会在 `dist/dev/mp-weixin` 或 `dist/build/mp-weixin` 目录生成微信小程序代码。

然后在微信开发者工具中：
1. 点击 `导入项目`
2. 选择编译后的目录：`tool-ai-uniapp/dist/dev/mp-weixin`
3. 填写 AppID
4. 点击 `导入`

### ⚠️ 方案三：临时创建 app.json（不推荐，仅用于测试）

如果暂时无法使用 HBuilderX，可以临时创建一个 `app.json` 文件：

```json
{
  "pages": [
    "pages/index/index",
    "pages/remove-logo/remove-logo",
    "pages/ai-avatar/ai-avatar",
    "pages/name-sign/name-sign",
    "pages/fortune/fortune",
    "pages/result/result",
    "pages/user/user"
  ],
  "window": {
    "navigationBarTextStyle": "black",
    "navigationBarTitleText": "AI工具箱",
    "navigationBarBackgroundColor": "#ffffff",
    "backgroundColor": "#f5f5f5"
  },
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
}
```

**注意**：这种方式只是临时方案，无法完全支持 uni-app 的所有功能，建议使用 HBuilderX 编译。

## 推荐流程

1. ✅ 使用 HBuilderX 打开项目
2. ✅ 配置 `manifest.json` 中的 AppID
3. ✅ 配置 `common/utils/request.js` 中的后端地址
4. ✅ 点击 `运行` → `运行到小程序模拟器` → `微信开发者工具`
5. ✅ 在微信开发者工具中调试

## 为什么需要编译？

uni-app 是一个跨平台框架，它使用 `pages.json` 和 `manifest.json` 来配置项目。当运行到微信小程序时，HBuilderX 会：
- 将 `pages.json` 转换为 `app.json`
- 将 Vue 组件转换为小程序组件
- 处理样式和资源文件
- 生成符合微信小程序规范的代码

所以**必须通过 HBuilderX 编译**，不能直接用微信开发者工具打开源码目录。





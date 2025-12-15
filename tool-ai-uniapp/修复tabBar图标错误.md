# 修复 tabBar 图标错误

## 问题

编译时出现错误：
```
["tabBar"]["list"][0]["iconPath"]: "static/tab-icons/tool.png" 未找到
["tabBar"]["list"][0]["selectedIconPath"]: "static/tab-icons/tool-active.png" 未找到
["tabBar"]["list"][1]["iconPath"]: "static/tab-icons/user.png" 未找到
["tabBar"]["list"][1]["selectedIconPath"]: "static/tab-icons/user-active.png" 未找到
```

## 原因

`pages.json` 中配置了 tabBar，但图标文件不存在。

## 解决方案

### 方案一：暂时移除 tabBar（已执行）

已暂时移除 `pages.json` 中的 tabBar 配置，项目现在可以正常编译。

**优点**：立即可以编译运行  
**缺点**：没有底部导航栏，需要通过其他方式切换页面

### 方案二：准备图标文件后恢复 tabBar

1. **准备图标文件**
   - 在 `static/tab-icons/` 目录下放置以下文件：
     - `tool.png` - 工具箱图标（未选中）
     - `tool-active.png` - 工具箱图标（选中）
     - `user.png` - 我的图标（未选中）
     - `user-active.png` - 我的图标（选中）

2. **图标要求**
   - 尺寸：81px × 81px（推荐）
   - 格式：PNG
   - 文件大小：建议每个不超过 40KB

3. **恢复 tabBar 配置**
   - 在 `pages.json` 中取消注释 tabBar 配置
   - 重新编译项目

## 如何获取图标

### 方法一：使用图标库

1. **iconfont（推荐）**
   - 访问：https://www.iconfont.cn/
   - 搜索：工具箱、用户、我的
   - 下载 PNG 格式，81px 尺寸

2. **iconify**
   - 访问：https://iconify.design/
   - 搜索相关图标
   - 导出为 PNG

### 方法二：自己设计

使用 Figma、Sketch 等设计工具设计图标。

### 方法三：临时占位图标

可以使用简单的文字或图形作为占位符：
- 工具箱：使用 "工具" 文字或工具图标
- 我的：使用 "我的" 文字或用户图标

## 当前状态

✅ tabBar 配置已暂时移除  
✅ 项目可以正常编译  
✅ 页面可以通过导航正常访问  

## 下一步

1. 准备图标文件（见 `static/tab-icons/README.md`）
2. 恢复 tabBar 配置
3. 重新编译测试

## 临时使用说明

在没有 tabBar 的情况下：
- 首页可以通过导航访问其他功能页面
- 个人中心页面可以通过导航访问
- 所有功能都可以正常使用，只是没有底部导航栏





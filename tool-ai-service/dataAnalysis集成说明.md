# dataAnalysis-backend 集成说明

## 概述

已成功将 Java 项目集成到 `dataAnalysis-backend` API，用于获取抖音和小红书的无水印视频链接。

## 架构设计

### 服务优先级

去水印服务采用三级优先级策略：

1. **优先级1：dataAnalysis服务**（推荐）
   - 调用 `dataAnalysis-backend` 项目的 API
   - 支持抖音和小红书
   - 配置项：`data-analysis.enabled=true`

2. **优先级2：stealer服务**（备选）
   - 调用 `stealer-master` 项目的 API
   - 配置项：`stealer.enabled=false`（默认关闭）

3. **优先级3：本地实现**（兜底）
   - 仅支持抖音
   - 使用 HTML 解析方式

### 调用流程

```
用户请求
  ↓
WatermarkService.removeWatermark()
  ↓
检测平台类型（抖音/小红书）
  ↓
尝试 dataAnalysis 服务
  ├─ 成功 → 返回视频URL
  └─ 失败 → 尝试 stealer 服务
      ├─ 成功 → 返回视频URL
      └─ 失败 → 尝试本地实现（仅抖音）
          ├─ 成功 → 返回视频URL
          └─ 失败 → 抛出异常
```

## 配置说明

### application.yml

```yaml
# DataAnalysis服务配置（dataAnalysis-backend项目）
data-analysis:
  # 是否启用dataAnalysis服务
  enabled: true
  # DataAnalysis服务的基础URL
  base-url: http://localhost:8000

# Stealer服务配置
stealer:
  # 是否启用stealer服务
  enabled: false
  # Stealer服务的基础URL
  base-url: http://localhost:8000
```

### 环境变量配置

如果需要动态配置，可以在启动时设置：

```bash
java -jar app.jar \
  --data-analysis.enabled=true \
  --data-analysis.base-url=http://localhost:8000
```

## API 调用说明

### dataAnalysis-backend API 端点

1. **通用接口**（自动识别平台）：
   ```
   POST /analyze
   ```

2. **抖音专用接口**：
   ```
   POST /analyze/douyin
   ```

3. **小红书专用接口**：
   ```
   POST /analyze/xiaohongshu
   ```

### 请求格式

```json
{
  "url": "社交媒体分享链接",
  "type": "png",
  "format": "json"
}
```

### 响应格式

`dataAnalysis-backend` 的响应格式需要根据实际实现调整。当前代码支持多种可能的响应格式：

- `data.video`
- `data.video_url`
- `data.url`
- `video`
- `video_url`
- `url`
- `data.videos[0]`
- `videos[0]`
- `data.play_url`
- `play_url`

如果响应格式不同，需要修改 `DataAnalysisServiceImpl.extractVideoUrlFromResponse()` 方法。

## 新增文件

1. **DataAnalysisService.java**
   - 服务接口定义
   - 位置：`com.example.tool.service.DataAnalysisService`

2. **DataAnalysisServiceImpl.java**
   - 服务实现类
   - 位置：`com.example.tool.service.impl.DataAnalysisServiceImpl`
   - 功能：
     - 调用 dataAnalysis-backend API
     - 解析响应获取视频URL
     - 错误处理和日志记录

## 修改的文件

1. **WatermarkServiceImpl.java**
   - 添加了 `dataAnalysisService` 依赖注入
   - 修改了 `removeWatermark()` 方法，增加三级优先级策略
   - 支持小红书链接处理

2. **application.yml**
   - 添加了 `data-analysis` 配置项
   - 调整了 `stealer` 配置的默认值

## 使用示例

### 测试 dataAnalysis 服务

1. **确保 dataAnalysis-backend 服务运行**：
   ```bash
   cd /Users/gongxuesong/Work/individual/工具/all/dataAnalysis-backend
   ./run_dev.sh
   ```

2. **测试 Java 服务**：
   ```bash
   # 启动 Java 服务
   cd /Users/gongxuesong/Work/individual/工具/all/tool-ai-service
   mvn spring-boot:run
   ```

3. **调用去水印接口**：
   ```bash
   curl -X POST "http://localhost:8080/tool/remove-logo" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer YOUR_TOKEN" \
     -d '{
       "shareUrl": "https://v.douyin.com/xxxxx/"
     }'
   ```

## 响应格式适配

如果 `dataAnalysis-backend` 的响应格式与预期不同，需要修改 `DataAnalysisServiceImpl.extractVideoUrlFromResponse()` 方法。

### 查看实际响应格式

1. 直接调用 dataAnalysis-backend API：
   ```bash
   curl -X POST "http://localhost:8000/analyze" \
     -H "Content-Type: application/json" \
     -d '{
       "url": "https://v.douyin.com/xxxxx/",
       "type": "png",
       "format": "json"
     }'
   ```

2. 查看响应结构，然后修改 `extractVideoUrlFromResponse()` 方法中的路径。

## 错误处理

- **dataAnalysis 服务不可用**：自动降级到 stealer 或本地实现
- **响应格式不匹配**：记录警告日志，抛出业务异常
- **网络超时**：30秒超时，抛出业务异常

## 日志记录

所有关键操作都会记录日志：

- `INFO`: 服务调用开始、成功获取视频URL
- `WARN`: 服务调用失败、响应格式异常
- `ERROR`: 服务调用异常

## 注意事项

1. **服务依赖**：确保 `dataAnalysis-backend` 服务正常运行
2. **响应格式**：根据实际响应格式调整提取逻辑
3. **超时设置**：当前设置为30秒，可根据需要调整
4. **错误处理**：服务失败会自动降级，不会影响用户体验

## 后续优化建议

1. **响应格式标准化**：与 dataAnalysis-backend 团队协商统一的响应格式
2. **缓存机制**：对相同链接的请求结果进行缓存
3. **重试机制**：添加失败重试逻辑
4. **监控告警**：添加服务健康检查和告警机制




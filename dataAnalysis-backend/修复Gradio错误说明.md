# dataAnalysis-backend Gradio SSL 超时错误修复

## 问题描述

运行 `python start_server.py` 时出现 SSL 握手超时错误：

```
httpx.ConnectTimeout: _ssl.c:1112: The handshake operation timed out
```

错误发生在 Gradio 的 analytics 模块后台线程中，尝试获取本地 IP 地址时。

## 问题原因

1. **Gradio Analytics**：Gradio 默认会在后台尝试连接 `api.gradio.app` 来发送分析数据
2. **SSL 握手超时**：由于网络问题或 SSL 配置问题，连接超时
3. **非致命错误**：这是后台线程的错误，不会影响主程序运行，但会产生噪音

## 解决方案

已在以下文件中添加环境变量来禁用 Gradio analytics：

1. **main.py**：在导入其他模块之前设置
2. **start_server.py**：在启动服务器之前设置

设置的环境变量：
```python
os.environ['GRADIO_ANALYTICS_ENABLED'] = 'False'
os.environ['GRADIO_SERVER_NAME'] = '127.0.0.1'
```

## 验证修复

重新启动服务：

```bash
cd /Users/gongxuesong/Work/individual/工具/all/dataAnalysis-backend
source .venv/bin/activate
python start_server.py
```

应该不再看到 SSL 超时错误。

## 其他可选方案

如果问题仍然存在，可以：

1. **在 shell 脚本中设置环境变量**：
   ```bash
   export GRADIO_ANALYTICS_ENABLED=False
   export GRADIO_SERVER_NAME=127.0.0.1
   python start_server.py
   ```

2. **在 run_dev.sh 中添加**：
   ```bash
   export GRADIO_ANALYTICS_ENABLED=False
   export GRADIO_SERVER_NAME=127.0.0.1
   ```

3. **创建 .env 文件**（如果项目支持）：
   ```
   GRADIO_ANALYTICS_ENABLED=False
   GRADIO_SERVER_NAME=127.0.0.1
   ```

## 注意事项

- 禁用 analytics 不会影响 Gradio 的功能
- 这只是禁用数据收集，不影响实际使用
- 如果项目中有多个入口点，确保都在导入 gradio 之前设置这些环境变量




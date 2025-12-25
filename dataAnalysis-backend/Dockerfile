# 使用 Python 3.12 slim 版本作为基础镜像，兼顾体积和兼容性
FROM python:3.12-slim

# 设置工作目录
WORKDIR /app

# 设置环境变量
ENV PYTHONDONTWRITEBYTECODE 1
ENV PYTHONUNBUFFERED 1
ENV APP_ENV production

# 安装必要的系统依赖 (针对 opencv, onnxruntime 等库)
# 1. 替换 Debian 软件源为清华镜像以加速下载
# 2. 替换 libgl1-mesa-glx 为 libgl1 (新版本 Debian 中的包名)
RUN sed -i 's/deb.debian.org/mirrors.tuna.tsinghua.edu.cn/g' /etc/apt/sources.list.d/debian.sources && \
    apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    libgl1 \
    libglib2.0-0 \
    libsm6 \
    libxext6 \
    libxrender-dev \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# 复制依赖文件
COPY requirements.txt .

# 安装 Python 依赖 (使用清华源，解决网络问题)
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple

# 复制项目代码
COPY . .

# 复制并重命名配置文件 (如果不存在)
RUN if [ ! -f config.ini ]; then cp config.template.ini config.ini; fi

# 创建日志目录
RUN mkdir -p logs

# 暴露端口
EXPOSE 8000

# 启动命令
# 使用 start_server.py 启动，或者直接使用 uvicorn
CMD ["python", "start_server.py", "--env", "production", "--host", "0.0.0.0", "--port", "8000"]

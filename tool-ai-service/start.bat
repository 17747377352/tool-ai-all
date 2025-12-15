@echo off
REM Spring Boot 启动脚本（Windows版本）
REM 从jar包同级目录读取配置文件
REM 使用方法: start.bat [配置文件名称]
REM 示例: start.bat application-dev.yml

REM 获取脚本所在目录（jar包所在目录）
set APP_HOME=%~dp0
set JAR_NAME=tool-ai-service-1.0.jar

REM 配置文件名称（默认 application-dev.yml）
if "%1"=="" (
    set CONFIG_NAME=application-dev.yml
) else (
    set CONFIG_NAME=%1
)
set CONFIG_FILE=%APP_HOME%%CONFIG_NAME%

REM 检查jar包是否存在
if not exist "%APP_HOME%%JAR_NAME%" (
    echo 错误: 找不到jar包 %APP_HOME%%JAR_NAME%
    pause
    exit /b 1
)

REM 检查配置文件是否存在
if not exist "%CONFIG_FILE%" (
    echo 警告: 配置文件 %CONFIG_FILE% 不存在，将使用jar包内默认配置
    set CONFIG_FILE=
)

REM 构建启动命令
if defined CONFIG_FILE (
    set JAVA_OPTS=-Dspring.config.location=file:%CONFIG_FILE%
) else (
    set JAVA_OPTS=
)

REM 启动应用
echo 正在启动应用...
java -jar %JAVA_OPTS% %APP_HOME%%JAR_NAME%

pause


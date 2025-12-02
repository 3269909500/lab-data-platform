@echo off
echo 正在停止网关服务...

REM 查找并停止占用8080端口的进程
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (
    for /f "tokens=2" %%b in ("%%a") do (
        echo 找到占用8080端口的进程: %%b
        taskkill /F /PID %%b
    )
)

timeout /t 3 /nobreak

echo 正在启动网关服务...
cd /d "D:\JAVA\daima\lab-data-platform\gateway-service"
java -jar target\gateway-service-1.0.0.jar

echo 网关服务已启动
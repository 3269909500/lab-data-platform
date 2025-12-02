@echo off
echo =========================================
echo 检查虚拟机Docker服务状态
echo =========================================
echo.

echo 1. 检查虚拟机网络连接...
ping -n 2 192.168.141.128 >nul
if %errorlevel% equ 0 (
    echo ✅ 虚拟机网络连接正常
) else (
    echo ❌ 虚拟机网络连接失败，请先启动虚拟机
    pause
    exit /b 1
)
echo.

echo 2. 检查Docker容器状态...
ssh root@192.168.141.128 "docker ps -a"
echo.

echo 3. 检查具体服务容器...
echo 检查Nacos容器:
ssh root@192.168.141.128 "docker ps | grep nacos"

echo.
echo 检查MySQL容器:
ssh root@192.168.141.128 "docker ps | grep mysql"

echo.
echo 检查Kafka容器:
ssh root@192.168.141.128 "docker ps | grep kafka"

echo.
echo 4. 检查端口占用情况...
ssh root@192.168.141.128 "netstat -tlnp | grep -E ':(3307|8849|9092)'"

echo.
echo =========================================
echo 检查完成
echo =========================================
pause
@echo off
:MENU
cls
echo =========================================
echo Docker服务管理工具
echo =========================================
echo.
echo 1. 检查虚拟机Docker服务状态
echo 2. 启动虚拟机Docker服务
echo 3. 停止虚拟机Docker服务
echo 4. 重启虚拟机Docker服务
echo 5. 查看服务日志
echo 6. 测试服务连接
echo 0. 退出
echo.
set /p choice=请选择操作 (0-6):

if "%choice%"=="1" goto CHECK
if "%choice%"=="2" goto START
if "%choice%"=="3" goto STOP
if "%choice%"=="4" goto RESTART
if "%choice%"=="5" goto LOGS
if "%choice%"=="6" goto TEST
if "%choice%"=="0" goto EXIT
goto MENU

:CHECK
echo 检查Docker服务状态...
call check-docker-services.bat
pause
goto MENU

:START
echo 启动Docker服务...
call remote-start-services.bat
pause
goto MENU

:STOP
echo 停止Docker服务...
ssh root@192.168.141.128 "docker stop mysql-lab nacos-lab kafka-lab zookeeper-lab"
echo 服务已停止
pause
goto MENU

:RESTART
echo 重启Docker服务...
ssh root@192.168.141.128 "docker restart mysql-lab nacos-lab kafka-lab zookeeper-lab"
echo 服务已重启
pause
goto MENU

:LOGS
echo 选择要查看的服务日志:
echo 1. MySQL
echo 2. Nacos
echo 3. Kafka
set /p log_choice=请选择 (1-3):

if "%log_choice%"=="1" (
    ssh root@192.168.141.128 "docker logs -f mysql-lab"
) else if "%log_choice%"=="2" (
    ssh root@192.168.141.128 "docker logs -f nacos-lab"
) else if "%log_choice%"=="3" (
    ssh root@192.168.141.128 "docker logs -f kafka-lab"
)
pause
goto MENU

:TEST
echo 测试服务连接...
echo.
echo 1. 测试MySQL连接:
ssh root@192.168.141.128 "mysql -h192.168.141.128 -P3307 -uroot -pRoot123!@# -e 'SELECT 1'"
echo.

echo 2. 测试Nacos连接:
curl -s http://192.168.141.128:8849/nacos/v1/console/health
echo.

echo 3. 测试Kafka连接:
ssh root@192.168.141.128 "docker exec kafka-lab kafka-broker-api-versions --bootstrap-server localhost:9092"
echo.
pause
goto MENU

:EXIT
echo 退出程序
exit /b 0
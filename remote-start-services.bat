@echo off
echo =========================================
echo 远程启动虚拟机Docker服务
echo =========================================
echo.

echo 1. 连接到虚拟机并启动Docker服务...
ssh root@192.168.141.128 "cd /root && bash start-docker-services.sh"

echo.
echo 2. 验证服务启动状态...
echo 检查MySQL连接:
ssh root@192.168.141.128 "docker exec mysql-lab mysql -uroot -pRoot123!@# -e 'SHOW DATABASES;'"

echo.
echo 检查Nacos状态:
curl -s http://192.168.141.128:8849/nacos/v1/console/health || echo "Nacos未就绪"

echo.
echo 3. 显示所有运行中的容器...
ssh root@192.168.141.128 "docker ps"

echo.
echo =========================================
echo 服务启动完成！
echo =========================================
pause
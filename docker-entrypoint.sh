#!/bin/sh

# 设置默认参数
DEFAULT_NACOS_SERVER="192.168.141.128:8849"
DEFAULT_MYSQL_SERVER="192.168.141.128:3307"
DEFAULT_KAFKA_SERVER="192.168.141.128:9092"

# 从环境变量读取配置，如果未设置则使用默认值
NACOS_SERVER=${NACOS_SERVER:-$DEFAULT_NACOS_SERVER}
MYSQL_SERVER=${MYSQL_SERVER:-$DEFAULT_MYSQL_SERVER}
KAFKA_SERVER=${KAFKA_SERVER:-$DEFAULT_KAFKA_SERVER}

echo "========================================="
echo "  实验室数据中台启动脚本"
echo "========================================="
echo "Nacos服务器: $NACOS_SERVER"
echo "MySQL服务器: $MYSQL_SERVER"
echo "Kafka服务器: $KAFKA_SERVER"
echo "========================================="

# 根据参数启动不同服务
case "$1" in
    auth)
        echo "启动认证服务..."
        java -jar -Dspring.cloud.nacos.discovery.server-addr=$NACOS_SERVER \
                   -Dspring.datasource.url=jdbc:mysql://$MYSQL_SERVER/lab_data_platform?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false \
                   app-auth.jar
        ;;
    system)
        echo "启动系统服务..."
        java -jar -Dspring.cloud.nacos.discovery.server-addr=$NACOS_SERVER \
                   -Dspring.datasource.url=jdbc:mysql://$MYSQL_SERVER/lab_data_platform?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false \
                   app-system.jar
        ;;
    monitor)
        echo "启动监测服务..."
        java -jar -Dspring.cloud.nacos.discovery.server-addr=$NACOS_SERVER \
                   -Dspring.datasource.url=jdbc:mysql://$MYSQL_SERVER/lab_data_platform?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false \
                   -Dspring.kafka.bootstrap-servers=$KAFKA_SERVER \
                   app-monitor.jar
        ;;
    gateway)
        echo "启动网关服务..."
        java -jar app-gateway.jar
        ;;
    all)
        echo "启动所有服务..."
        # 后台启动所有服务
        nohup java -jar -Dspring.cloud.nacos.discovery.server-addr=$NACOS_SERVER \
                        -Dspring.datasource.url=jdbc:mysql://$MYSQL_SERVER/lab_data_platform?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false \
                        app-auth.jar > auth.log 2>&1 &

        nohup java -jar -Dspring.cloud.nacos.discovery.server-addr=$NACOS_SERVER \
                        -Dspring.datasource.url=jdbc:mysql://$MYSQL_SERVER/lab_data_platform?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false \
                        app-system.jar > system.log 2>&1 &

        nohup java -jar -Dspring.cloud.nacos.discovery.server-addr=$NACOS_SERVER \
                        -Dspring.datasource.url=jdbc:mysql://$MYSQL_SERVER/lab_data_platform?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false \
                        -Dspring.kafka.bootstrap-servers=$KAFKA_SERVER \
                        app-monitor.jar > monitor.log 2>&1 &

        nohup java -jar app-gateway.jar > gateway.log 2>&1 &

        echo "所有服务已启动，查看日志:"
        echo "认证服务: tail -f auth.log"
        echo "系统服务: tail -f system.log"
        echo "监测服务: tail -f monitor.log"
        echo "网关服务: tail -f gateway.log"
        ;;
    *)
        echo "用法: $0 {auth|system|monitor|gateway|all}"
        echo "  auth   - 启动认证服务 (端口8081)"
        echo "  system - 启动系统服务 (端口8082)"
        echo "  monitor- 启动监测服务 (端口8083)"
        echo "  gateway- 启动网关服务 (端口8080)"
        echo "  all    - 启动所有服务"
        exit 1
esac
#!/bin/bash

echo "=========================================="
echo "启动虚拟机Docker服务"
echo "=========================================="

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "Docker未运行，正在启动Docker..."
    systemctl start docker
    systemctl enable docker
    sleep 3
fi

# 启动MySQL容器
echo "启动MySQL容器..."
docker start mysql-lab || docker run -d \
    --name mysql-lab \
    -p 3307:3306 \
    -e MYSQL_ROOT_PASSWORD=Root123!@# \
    -e MYSQL_DATABASE=lab_data_platform \
    -v mysql_data:/var/lib/mysql \
    mysql:8.0

# 启动Nacos容器
echo "启动Nacos容器..."
docker start nacos-lab || docker run -d \
    --name nacos-lab \
    -p 8849:8848 \
    -p 9848:9848 \
    -e MODE=standalone \
    -e SPRING_DATASOURCE_PLATFORM=mysql \
    -e MYSQL_SERVICE_HOST=mysql \
    -e MYSQL_SERVICE_PORT=3306 \
    -e MYSQL_SERVICE_DB_NAME=nacos \
    -e MYSQL_SERVICE_USER=root \
    -e MYSQL_SERVICE_PASSWORD=Root123!@# \
    -v nacos_logs:/home/nacos/logs \
    nacos/nacos-server:v2.2.0

# 启动Kafka容器
echo "启动Kafka容器..."
docker start kafka-lab || docker run -d \
    --name kafka-lab \
    -p 9092:9092 \
    -e KAFKA_BROKER_ID=1 \
    -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
    -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://192.168.141.128:9092 \
    -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
    confluentinc/cp-kafka:latest

# 启动ZooKeeper容器（Kafka依赖）
echo "启动ZooKeeper容器..."
docker start zookeeper-lab || docker run -d \
    --name zookeeper-lab \
    -p 2181:2181 \
    confluentinc/cp-zookeeper:latest \
    zookeeper

echo ""
echo "等待服务启动..."
sleep 10

echo ""
echo "检查容器状态..."
docker ps

echo ""
echo "检查端口监听..."
netstat -tlnp | grep -E ':(3307|8849|9092|2181)'

echo ""
echo "=========================================="
echo "Docker服务启动完成"
echo "=========================================="

# 显示连接信息
echo ""
echo "连接信息:"
echo "MySQL: 192.168.141.128:3307"
echo "Nacos: 192.168.141.128:8849"
echo "Nacos Web: http://192.168.141.128:8849/nacos"
echo "Kafka: 192.168.141.128:9092"
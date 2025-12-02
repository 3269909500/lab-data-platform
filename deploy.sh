#!/bin/bash

echo "========================================="
echo "  实验室数据中台 Docker 部署脚本"
echo "========================================="

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker未运行，请先启动Docker"
    exit 1
fi

# 检查网络是否存在
if ! docker network ls | grep -q "lab-network"; then
    echo "⚠️  lab-network不存在，尝试创建..."
    docker network create lab-network || echo "网络创建失败，使用默认网络"
fi

# 创建日志目录
mkdir -p logs

# 选择部署模式
echo "请选择部署模式:"
echo "1) 构建并启动所有服务"
echo "2) 重新构建镜像"
echo "3) 启动调试模式（仅启动monitor服务）"
echo "4) 查看服务状态"
echo "5) 查看日志"
echo "6) 停止服务"
echo "7) 完全清理（删除镜像和容器）"

read -p "请输入选项 (1-7): " choice

case $choice in
    1)
        echo "🚀 开始构建并启动所有服务..."
        docker-compose -f app-compose.yml up --build -d
        echo "✅ 服务启动中，请稍等..."
        sleep 10
        docker-compose -f app-compose.yml ps
        ;;
    2)
        echo "🔨 重新构建镜像..."
        docker-compose -f app-compose.yml build --no-cache
        ;;
    3)
        echo "🐛 启动调试模式..."
        docker-compose -f app-compose.yml --profile debug up -d monitor-service
        ;;
    4)
        echo "📊 查看服务状态..."
        docker-compose -f app-compose.yml ps
        ;;
    5)
        echo "📋 查看日志..."
        docker-compose -f app-compose.yml logs -f --tail=100
        ;;
    6)
        echo "🛑 停止服务..."
        docker-compose -f app-compose.yml down
        ;;
    7)
        echo "🧹 完全清理..."
        docker-compose -f app-compose.yml down -v --rmi all
        docker system prune -f
        ;;
    *)
        echo "❌ 无效选项"
        exit 1
esac

echo "========================================="
echo "  操作完成"
echo "========================================="
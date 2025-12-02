#!/bin/bash

echo "========================================="
echo "  实验室数据中台服务测试"
echo "========================================="

GATEWAY_URL="http://localhost:8080"
BASE_URL="http://localhost"

test_service() {
    local service_name=$1
    local port=$2
    local endpoint=$3

    echo -n "测试 $service_name (端口$port)... "

    if curl -s --connect-timeout 5 "$BASE_URL:$port$endpoint" > /dev/null; then
        echo "✅ 正常"
        return 0
    else
        echo "❌ 失败"
        return 1
    fi
}

# 测试各个服务
echo "测试各个服务健康状态..."

test_service "认证服务" 8081 "/actuator/health"
test_service "系统服务" 8082 "/actuator/health"
test_service "监测服务" 8083 "/actuator/health"
test_service "网关服务" 8080 "/actuator/health"

echo ""
echo "测试网关路由..."

# 测试通过网关访问各服务
test_gateway_route() {
    local path=$1
    local description=$2

    echo -n "测试网关路由 $description... "

    if curl -s --connect-timeout 5 "$GATEWAY_URL$path" > /dev/null; then
        echo "✅ 正常"
        return 0
    else
        echo "❌ 失败"
        return 1
    fi
}

test_gateway_route "/api/auth/actuator/health" "认证服务"
test_gateway_route "/api/laboratory/actuator/health" "系统服务"
test_gateway_route "/api/lab-monitor/actuator/health" "监测服务"

echo ""
echo "========================================="
echo "  测试完成"
echo "========================================="
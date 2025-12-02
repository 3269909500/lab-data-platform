# 阶段1: 构建应用
FROM maven:3.8.4-openjdk-8 AS builder

WORKDIR /app
COPY pom.xml .
COPY auth-service ./auth-service/
COPY common ./common/
COPY system-service ./system-service/
COPY monitor-service ./monitor-service/
COPY gateway-service ./gateway-service/

# 构建项目
RUN mvn clean package -DskipTests

# 阶段2: 运行应用
FROM openjdk:8-jre-alpine

WORKDIR /app

# 复制依赖和应用JAR
COPY --from=builder /app/auth-service/target/*.jar app-auth.jar
COPY --from=builder /app/system-service/target/*.jar app-system.jar
COPY --from=builder /app/monitor-service/target/*.jar app-monitor.jar
COPY --from=builder /app/gateway-service/target/*.jar app-gateway.jar

# 设置时区
RUN apk add --no-cache tzdata && \
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

# 暴露端口
EXPOSE 8081 8082 8083 8080

# 启动脚本
COPY docker-entrypoint.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

ENTRYPOINT ["docker-entrypoint.sh"]
package com.sewage.monitor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.sewage.monitor", "com.sewage.common"})
@EnableDiscoveryClient
@MapperScan("com.sewage.monitor.mapper")  // 添加这个注解
public class MonitorServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MonitorServiceApplication.class, args);
        System.out.println("========== 监控服务启动成功 ==========");
    }
}
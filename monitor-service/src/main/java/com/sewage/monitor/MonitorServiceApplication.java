package com.sewage.monitor;

/**
 * 监控服务启动类 - 实验室环境监控服务
 */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.sewage.monitor", "com.sewage.common"})
@EnableDiscoveryClient
@EnableScheduling
public class MonitorServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MonitorServiceApplication.class, args);
        System.out.println("========== 监控服务启动成功 ==========");
    }
}
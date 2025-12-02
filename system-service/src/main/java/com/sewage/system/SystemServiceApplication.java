package com.sewage.system;

/**
 * 系统服务启动类 - 实验室信息管理服务
 */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.sewage.system", "com.sewage.common"})
@EnableDiscoveryClient
public class SystemServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SystemServiceApplication.class, args);
        System.out.println("========== 系统管理服务启动成功 ==========");
    }
}
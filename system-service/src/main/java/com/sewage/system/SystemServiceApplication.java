package com.sewage.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.sewage.system", "com.sewage.common"})
@EnableDiscoveryClient
@MapperScan("com.sewage.system.mapper")
public class SystemServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SystemServiceApplication.class, args);
        System.out.println("========== 系统管理服务启动成功 ==========");
    }
}
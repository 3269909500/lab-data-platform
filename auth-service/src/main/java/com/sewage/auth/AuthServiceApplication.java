package com.sewage.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 认证服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.sewage.auth", "com.sewage.common"})
@EnableDiscoveryClient
@MapperScan("com.sewage.auth.mapper")
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
        System.out.println("========== 认证服务启动成功 ==========");
    }
}
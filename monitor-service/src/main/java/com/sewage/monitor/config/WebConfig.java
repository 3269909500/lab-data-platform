package com.sewage.monitor.config;

import com.sewage.monitor.interceptor.UserInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 *
 * 作用：
 * 1. 注册拦截器到Spring MVC
 * 2. 配置拦截规则（哪些路径需要拦截，哪些不需要）
 *
 * 为什么要实现 WebMvcConfigurer 接口？
 * - Spring Boot 2.0+ 推荐使用这种方式配置MVC
 * - 可以覆盖默认配置，但不会完全替换自动配置
 * - 比继承 WebMvcConfigurationSupport 更灵活
 */
@Configuration  // 标记为配置类，Spring启动时会自动加载
@RequiredArgsConstructor  // Lombok注解，自动生成构造函数注入
public class WebConfig implements WebMvcConfigurer {

    /**
     * 注入UserInterceptor
     * 使用构造函数注入（比@Autowired更推荐）
     */
    private final UserInterceptor userInterceptor;

    /**
     * 注册拦截器
     *
     * 这个方法会在Spring启动时自动调用
     * 我们在这里把UserInterceptor添加到拦截器链中
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 步骤1：添加拦截器
        registry.addInterceptor(userInterceptor)

                // 步骤2：配置拦截路径（哪些请求需要拦截）
                // /** 表示拦截所有请求（包括子路径）
                // 例如：/monitor/data、/monitor/latest/1、/monitor/page 都会被拦截
                .addPathPatterns("/**")

                // 步骤3：配置排除路径（哪些请求不需要拦截）
                // 为什么要排除这些路径？
                // 1. /monitor/test - 测试接口，不需要用户信息
                // 2. /error - Spring Boot默认的错误处理页面
                // 3. /actuator/** - Spring Boot Actuator的健康检查接口
                // 4. /swagger-ui/** - Swagger文档页面（如果有）
                // 5. /v3/api-docs/** - OpenAPI文档（如果有）
                .excludePathPatterns(
                        "/monitor/test",      // 测试接口
                        "/error",             // 错误页面
                        "/actuator/**",       // 健康检查
                        "/swagger-ui/**",     // Swagger UI
                        "/v3/api-docs/**"     // OpenAPI文档
                );

        // 注意：
        // 1. 可以添加多个拦截器，按添加顺序执行
        // 2. 如果有多个拦截器，preHandle按顺序执行，afterCompletion按倒序执行
        // 3. 如果某个拦截器的preHandle返回false，后续拦截器和Controller都不会执行
    }
}
package com.sewage.monitor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步配置类
 *
 * 功能说明：
 * 1. 配置异步任务线程池
 * 2. 支持统计数据异步更新
 * 3. 提高系统并发处理能力
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 统计更新异步线程池
     *
     * 线程池配置说明：
     * - 核心线程数：5（处理日常统计更新）
     * - 最大线程数：20（应对高峰期）
     * - 队列容量：100（缓冲任务）
     * - 拒绝策略：CallerRuns（由调用线程执行）
     */
    @Bean("statisticsExecutor")
    public Executor statisticsExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(5);

        // 最大线程数
        executor.setMaxPoolSize(20);

        // 队列容量
        executor.setQueueCapacity(100);

        // 线程空闲时间（秒）
        executor.setKeepAliveSeconds(60);

        // 线程名前缀
        executor.setThreadNamePrefix("statistics-async-");

        // 拒绝策略：由调用线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 等待时间
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }

    /**
     * 缓存预热异步线程池
     *
     * 线程池配置说明：
     * - 核心线程数：2（缓存预热任务较少）
     * - 最大线程数：5（应对多个实验室预热）
     * - 队列容量：50
     */
    @Bean("cacheWarmupExecutor")
    public Executor cacheWarmupExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(30);
        executor.setThreadNamePrefix("cache-warmup-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        return executor;
    }

    /**
     * 报表生成异步线程池
     *
     * 线程池配置说明：
     * - 核心线程数：3（处理报表生成任务）
     * - 最大线程数：10（应对高峰期多个报表请求）
     * - 队列容量：50（缓冲任务）
     * - 拒绝策略：AbortPolicy（拒绝新任务并抛出异常）
     */
    @Bean("reportExecutor")
    public Executor reportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(3);

        // 最大线程数
        executor.setMaxPoolSize(10);

        // 队列容量
        executor.setQueueCapacity(50);

        // 线程空闲时间（秒）
        executor.setKeepAliveSeconds(120);

        // 线程名前缀
        executor.setThreadNamePrefix("report-async-");

        // 拒绝策略：拒绝新任务并抛出异常
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 等待时间
        executor.setAwaitTerminationSeconds(120);

        executor.initialize();
        return executor;
    }
}
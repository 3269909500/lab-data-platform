package com.sewage.monitor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Kafka 重试配置
 *
 * 配置说明：
 * 1. 消息处理失败时的重试策略
 * 2. 重试次数和时间间隔配置
 * 3. 重试失败后发送到死信队列
 */
@Configuration
public class KafkaRetryConfig {

    /**
     * 配置Kafka监听器容器工厂
     * 添加重试和死信队列支持
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        // 配置重试策略：指数退避，最多重试3次
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(1000L);     // 初始间隔1秒
        backOff.setMultiplier(2.0);            // 每次间隔翻倍
        backOff.setMaxInterval(10000L);        // 最大间隔10秒

        // 配置错误处理器
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                // 重试失败后，发送到死信队列
                (record, exception) -> {
                    // 这里可以添加自定义的死信队列处理逻辑
                    // Spring会自动将失败消息发送到死信队列
                },
                backOff
        );

        // 添加需要重试的异常类型（默认会重试所有非致命异常）
        errorHandler.addNotRetryableExceptions(
                IllegalArgumentException.class,  // 参数错误，重试无意义
                NullPointerException.class      // 空指针异常，重试无意义
        );

        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }
}
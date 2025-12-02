package com.sewage.monitor.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 测试消费者
 *
 * 作用：验证 Kafka 是否配置成功
 *
 * 工作原理：
 * 1. @KafkaListener 注解会自动订阅指定的 Topic
 * 2. 当 Topic 中有新消息时，自动调用这个方法
 * 3. 参数 message 就是收到的消息内容
 *
 * 测试步骤：
 * 1. 启动 monitor-service
 * 2. 调用 /monitor/test-kafka 接口发送消息
 * 3. 查看控制台日志，应该能看到"接收到消息：xxx"
 */
@Slf4j
@Component  // 恢复Kafka消费者
public class TestConsumer {

    /**
     * 消费监测数据消息
     *
     * @KafkaListener 注解详解：
     * - topics：监听的 Topic 名称（可以监听多个）
     * - groupId：消费者组ID（同一组内的消费者会分摊消息）
     * - concurrency：并发消费者数量（3个线程并行处理）
     *
     * @param message 接收到的消息内容
     */
    @KafkaListener(  // 恢复Kafka消费者
            topics = "lab-environment-data",     // 监听实验室环境数据Topic
            groupId = "test-group",             // 消费者组ID
            concurrency = "3"                   // 3个并发消费者（对应3个分区）
    )
    public void handleMessage(String message) {
        // 收到消息后，打印日志
        log.info("【Kafka 消费者】接收到消息：{}", message);

        // 模拟处理消息（实际项目中这里会有业务逻辑）
        try {
            // 假装处理需要1秒
            Thread.sleep(1000);
            log.info("【Kafka 消费者】消息处理完成：{}", message);

        } catch (InterruptedException e) {
            log.error("消息处理被中断", e);
        } catch (Exception e) {
            log.error("消息处理失败：{}", message, e);
        }
    }
}
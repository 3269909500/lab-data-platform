package com.sewage.monitor.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

/**
 * 死信队列消费者
 *
 * 功能：
 * 1. 处理失败的消息会自动进入死信队列
 * 2. 可以对死信消息进行人工处理或重新处理
 * 3. 记录详细的错误信息便于分析
 */
@Slf4j
@Component  // 恢复Kafka消费者
@RequiredArgsConstructor
public class DeadLetterQueueConsumer {

    private final ObjectMapper objectMapper;

    /**
     * 处理实验室环境数据死信队列消息
     */
    @KafkaListener(  // 恢复Kafka消费者
            topics = "lab-environment-data-dlq",
            groupId = "dlq-lab-data-group",
            concurrency = "1"
    )
    public void handleLabDataDlq(ConsumerRecord<String, String> record) {
        try {
            log.error("💀 [死信队列-实验室数据] 收到失败消息 - Partition: {}, Offset: {}, Key: {}",
                    record.partition(), record.offset(), record.key());

            String message = record.value();
            log.error("💀 [死信队列-实验室数据] 消息内容: {}", message);

            // TODO: 这里可以添加死信消息的处理逻辑
            // 1. 保存到专门的失败消息表
            // 2. 发送告警通知管理员
            // 3. 尝试重新处理（如果可恢复）
            // 4. 人工干预处理

        } catch (Exception e) {
            log.error("💀 [死信队列-实验室数据] 处理死信消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理告警死信队列消息
     */
    @KafkaListener(  // 恢复Kafka消费者
            topics = "lab-alarm-data-dlq",
            groupId = "dlq-lab-alarm-group",
            concurrency = "1"
    )
    public void handleLabAlarmDlq(ConsumerRecord<String, String> record) {
        try {
            log.error("🚨 [死信队列-告警] 收到失败消息 - Partition: {}, Offset: {}, Key: {}",
                    record.partition(), record.offset(), record.key());

            String message = record.value();
            log.error("🚨 [死信队列-告警] 消息内容: {}", message);

            // TODO: 这里可以添加死信消息的处理逻辑
            // 1. 保存到专门的失败消息表
            // 2. 发送告警通知管理员
            // 3. 尝试重新处理（如果可恢复）
            // 4. 人工干预处理

        } catch (Exception e) {
            log.error("🚨 [死信队列-告警] 处理死信消息失败: {}", e.getMessage(), e);
        }
    }
}
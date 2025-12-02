package com.sewage.monitor.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sewage.monitor.config.KafkaConfig;
import com.sewage.monitor.entity.LabEnvironmentData;
import com.sewage.monitor.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 实验室统计消费者
 *
 * 功能：
 * 1. 接收实验室环境数据
 * 2. 实时更新统计数据
 * 3. 更新日统计表
 */
@Slf4j
@Component  // 恢复Kafka消费者
@RequiredArgsConstructor
public class StatisticsConsumer {

    private final StatisticsService statisticsService;
    private final ObjectMapper objectMapper;

    /**
     * 监听实验室环境数据 Topic
     *
     * 注意：
     * - 与 LabEnvironmentDataConsumer 使用不同的 groupId
     * - 这样可以独立消费同一份数据，互不影响
     */
    @KafkaListener(  // 恢复Kafka消费者
            topics = KafkaConfig.TOPIC_LAB_ENV_DATA,
            groupId = "lab-statistics-consumer-group",  // 不同的 groupId
            concurrency = "2"
    )
    public void consume(ConsumerRecord<String, String> record) {
        try {
            // 1. 打印消息信息
            log.debug("📨 [统计消费者] 收到消息 - Partition: {}, Offset: {}",
                    record.partition(), record.offset());

            // 2. 解析实验室环境数据
            String message = record.value();
            LabEnvironmentData environmentData = objectMapper.readValue(message, LabEnvironmentData.class);

            log.debug("📊 [统计消费者] 处理数据 - 实验室: {}, 时间: {}",
                    environmentData.getLabName(), environmentData.getMonitorTime());

            // 3. 更新统计数据（实时统计）
            statisticsService.updateStatistics(environmentData);

            log.debug("✅ [统计消费者] 统计已更新 - 实验室: {}",
                    environmentData.getLabName());

        } catch (Exception e) {
            log.error("❌ [统计消费者] 消息处理失败: {}", e.getMessage(), e);
        }
    }
}
package com.sewage.monitor.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sewage.monitor.config.KafkaConfig;
import com.sewage.monitor.entity.LabEnvironmentData;
import com.sewage.monitor.service.AlarmService;
import com.sewage.monitor.service.LabEnvironmentDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 实验室环境数据消费者
 *
 * 功能：
 * 1. 接收实验室环境数据
 * 2. 保存数据到数据库
 * 3. 触发告警检查
 */
@Slf4j
@Component  // 恢复Kafka消费者
@RequiredArgsConstructor
public class LabEnvironmentDataConsumer {

    private final LabEnvironmentDataService labEnvironmentDataService;
    private final AlarmService alarmService;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 监听实验室环境数据 Topic
     *
     * 消费者组说明：
     * - lab-data-consumer-group：专门处理数据持久化
     * - 与统计消费者使用不同的组，可以独立消费
     */
    @KafkaListener(
            topics = KafkaConfig.TOPIC_LAB_ENV_DATA,
            groupId = "lab-data-consumer-group",
            concurrency = "3"  // 3个并发消费者，对应5个分区
    )
    public void consume(ConsumerRecord<String, String> record) {
        try {
            // 1. 打印消息信息
            log.info("📨 [环境数据消费者] 收到消息 - Partition: {}, Offset: {}, Key: {}",
                    record.partition(), record.offset(), record.key());

            // 2. 解析实验室环境数据
            String message = record.value();
            LabEnvironmentData environmentData = objectMapper.readValue(message, LabEnvironmentData.class);

            log.info("📊 [环境数据消费者] 处理数据 - 实验室: {}, 时间: {}, 温度: {}°C",
                    environmentData.getLabName(), environmentData.getMonitorTime(), environmentData.getTemperature());

            // 3. 保存数据到数据库
            labEnvironmentDataService.saveMonitorData(environmentData);
            log.info("💾 [环境数据消费者] 数据已保存到数据库 - LabId: {}", environmentData.getLabId());

            // 4. 触发告警检查
            alarmService.checkAndSendAlarm(environmentData);
            log.info("🚨 [环境数据消费者] 告警检查完成 - LabId: {}", environmentData.getLabId());

            log.info("✅ [环境数据消费者] 消息处理完成 - 实验室: {}", environmentData.getLabName());

        } catch (Exception e) {
            log.error("❌ [环境数据消费者] 消息处理失败: {}", e.getMessage(), e);

            // 发送失败消息到死信队列
            try {
                kafkaTemplate.send(KafkaConfig.TOPIC_LAB_ENV_DATA_DLQ, record.key(), record.value());
                log.error("💀 [环境数据消费者] 已发送到死信队列 - Key: {}", record.key());
            } catch (Exception dlqException) {
                log.error("❌ [环境数据消费者] 发送到死信队列失败: {}", dlqException.getMessage(), dlqException);
            }
        }
    }
}
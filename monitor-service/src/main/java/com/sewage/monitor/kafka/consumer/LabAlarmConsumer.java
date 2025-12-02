package com.sewage.monitor.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sewage.monitor.config.KafkaConfig;
import com.sewage.monitor.entity.LabAlarm;
import com.sewage.monitor.mapper.LabAlarmMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 实验室告警消费者
 *
 * 功能：
 * 1. 接收告警数据
 * 2. 保存告警到数据库
 * 3. 可扩展：发送通知、触发其他处理流程
 */
@Slf4j
@Component  // 恢复Kafka消费者
@RequiredArgsConstructor
public class LabAlarmConsumer {

    private final LabAlarmMapper labAlarmMapper;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 监听实验室告警数据 Topic
     */
    @KafkaListener(
            topics = KafkaConfig.TOPIC_LAB_ALARM,
            groupId = "lab-alarm-consumer-group",
            concurrency = "2"  // 2个并发消费者，对应2个分区
    )
    public void consume(ConsumerRecord<String, String> record) {
        try {
            // 1. 打印消息信息
            log.info("📨 [告警消费者] 收到消息 - Partition: {}, Offset: {}, Key: {}",
                    record.partition(), record.offset(), record.key());

            // 2. 解析告警数据
            String message = record.value();
            LabAlarm alarm = objectMapper.readValue(message, LabAlarm.class);

            log.info("🚨 [告警消费者] 处理告警 - 实验室: {}, 类型: {}, 级别: {}, 消息: {}",
                    alarm.getLabName(), alarm.getAlarmType(),
                    alarm.getAlarmLevel(), alarm.getAlarmMessage());

            // 3. 保存告警到数据库
            int result = labAlarmMapper.insert(alarm);
            if (result > 0) {
                log.info("💾 [告警消费者] 告警已保存到数据库 - 告警ID: {}, LabId: {}",
                        alarm.getId(), alarm.getLabId());
            } else {
                log.error("❌ [告警消费者] 告警保存失败 - LabId: {}, AlarmType: {}",
                        alarm.getLabId(), alarm.getAlarmType());
            }

            // 4. TODO: 可以在这里添加其他告警处理逻辑
            // - 发送邮件/短信通知
            // - 推送到WebSocket
            // - 调用第三方告警平台API
            // - 记录操作日志

            log.info("✅ [告警消费者] 告警处理完成 - 告警ID: {}", alarm.getId());

        } catch (Exception e) {
            log.error("❌ [告警消费者] 消息处理失败: {}", e.getMessage(), e);

            // 发送失败消息到死信队列
            try {
                kafkaTemplate.send(KafkaConfig.TOPIC_LAB_ALARM_DLQ, record.key(), record.value());
                log.error("💀 [告警消费者] 已发送到死信队列 - Key: {}", record.key());
            } catch (Exception dlqException) {
                log.error("❌ [告警消费者] 发送到死信队列失败: {}", dlqException.getMessage(), dlqException);
            }
        }
    }
}
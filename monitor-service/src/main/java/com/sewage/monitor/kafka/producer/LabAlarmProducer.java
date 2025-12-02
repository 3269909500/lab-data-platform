package com.sewage.monitor.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sewage.monitor.config.KafkaConfig;
import com.sewage.monitor.entity.LabAlarm;
import com.sewage.monitor.entity.LabEnvironmentData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import java.util.concurrent.CompletableFuture;
import org.springframework.stereotype.Component;

/**
 * 实验室告警生产者
 * 负责将实验室告警数据发送到Kafka
 */
@Slf4j
@Component
@EnableKafka
@RequiredArgsConstructor
public class LabAlarmProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 创建ObjectMapper实例以避免循环引用
     */
    private final ObjectMapper objectMapper = new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    /**
     * 发送实验室告警数据
     */
    public void sendAlarm(LabAlarm alarm) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(alarm);
            String key = alarm.getLabId() + ":" + alarm.getAlarmType();

            log.info("🚨 实验室告警异步发送成功 - LabId: {}, AlarmType: {}",
                    alarm.getLabId(), alarm.getAlarmType());

            kafkaTemplate.send(KafkaConfig.TOPIC_LAB_ALARM, key, jsonMessage)
                    .addCallback(
                            result -> log.debug("🚨 实验室告警异步发送成功 - LabId: {}, AlarmType: {}",
                                    alarm.getLabId(), alarm.getAlarmType()),
                            failure -> log.error("❌ 实验室告警异步发送失败 - LabId: {}, AlarmType: {}, Error: {}",
                                    alarm.getLabId(), alarm.getAlarmType(), failure.getMessage())
                    );
        } catch (JsonProcessingException e) {
            log.error("❌ 实验室告警JSON序列化失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 批量发送实验室告警数据
     */
    public void sendAlarmBatch(java.util.List<LabAlarm> alarmList) {
        if (alarmList == null || alarmList.isEmpty()) {
            log.warn("⚠️ 实验室告警数据列表为空，跳过发送");
            return;
        }

        log.info("📤 开始批量发送实验室告警，数量: {}", alarmList.size());

        alarmList.forEach(this::sendAlarm);

        log.info("📤 实验室告警批量发送完成，数量: {}", alarmList.size());
    }
}
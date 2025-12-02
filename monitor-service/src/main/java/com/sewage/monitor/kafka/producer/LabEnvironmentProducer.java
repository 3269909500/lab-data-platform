package com.sewage.monitor.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sewage.monitor.config.KafkaConfig;
import com.sewage.monitor.entity.LabEnvironmentData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * 实验室环境数据生产者
 * 负责将实验室环境数据发送到Kafka
 * 对应原WaterDataProducer，改造为实验室环境数据生产者
 */
@Slf4j
@Component  // 恢复Kafka生产者
@RequiredArgsConstructor
public class LabEnvironmentProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    // 提供对KafkaTemplate的访问（用于测试）
    public KafkaTemplate<String, String> getKafkaTemplate() {
        return kafkaTemplate;
    }

    /**
     * 发送单条实验室环境数据
     *
     * @param data 环境数据
     */
    public void sendEnvironmentData(LabEnvironmentData data) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(data);
            // 使用实验室ID作为key，保证同一实验室的数据有序
            String key = String.valueOf(data.getLabId());

            ListenableFuture<SendResult<String, String>> future = kafkaTemplate
                    .send(KafkaConfig.TOPIC_LAB_ENV_DATA, key, jsonMessage);

            future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                @Override
                public void onSuccess(SendResult<String, String> result) {
                    log.debug("✅ 实验室环境数据发送成功 - Topic: {}, LabId: {}, Partition: {}, Offset: {}",
                            KafkaConfig.TOPIC_LAB_ENV_DATA, data.getLabId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }

                @Override
                public void onFailure(Throwable throwable) {
                    log.error("❌ 实验室环境数据发送失败 - Topic: {}, LabId: {}, Error: {}",
                            KafkaConfig.TOPIC_LAB_ENV_DATA, data.getLabId(), throwable.getMessage(), throwable);
                }
            });

        } catch (JsonProcessingException e) {
            log.error("❌ 实验室环境数据JSON序列化失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 异步发送实验室环境数据（不等待结果）
     *
     * @param data 环境数据
     */
    public void sendEnvironmentDataAsync(LabEnvironmentData data) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(data);
            String key = String.valueOf(data.getLabId());

            kafkaTemplate.send(KafkaConfig.TOPIC_LAB_ENV_DATA, key, jsonMessage)
                    .addCallback(
                            result -> log.debug("✅ 实验室环境数据异步发送成功 - LabId: {}", data.getLabId()),
                            failure -> log.error("❌ 实验室环境数据异步发送失败 - LabId: {}, Error: {}",
                                    data.getLabId(), failure.getMessage())
                    );

        } catch (JsonProcessingException e) {
            log.error("❌ 实验室环境数据JSON序列化失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 批量发送实验室环境数据
     *
     * @param dataList 环境数据列表
     */
    public void sendEnvironmentDataBatch(java.util.List<LabEnvironmentData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            log.warn("⚠️ 实验室环境数据列表为空，跳过发送");
            return;
        }

        log.info("📤 开始批量发送实验室环境数据，数量: {}", dataList.size());

        dataList.forEach(this::sendEnvironmentDataAsync);

        log.info("📤 实验室环境数据批量发送完成，数量: {}", dataList.size());
    }

    /**
     * 同步发送实验室环境数据（等待发送完成）
     *
     * @param data 环境数据
     * @return 是否发送成功
     */
    public boolean sendEnvironmentDataSync(LabEnvironmentData data) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(data);
            String key = String.valueOf(data.getLabId());

            SendResult<String, String> result = kafkaTemplate
                    .send(KafkaConfig.TOPIC_LAB_ENV_DATA, key, jsonMessage)
                    .get(); // 同步等待结果

            log.info("✅ 实验室环境数据同步发送成功 - LabId: {}, Partition: {}, Offset: {}",
                    data.getLabId(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());

            return true;

        } catch (JsonProcessingException e) {
            log.error("❌ 实验室环境数据JSON序列化失败: {}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("❌ 实验室环境数据同步发送失败 - LabId: {}, Error: {}",
                    data.getLabId(), e.getMessage(), e);
            return false;
        }
    }
}
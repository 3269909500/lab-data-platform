package com.sewage.monitor.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka 配置类
 * 自动创建 Topics - 实验室数据中台
 */
@Configuration  // 恢复Kafka配置
public class KafkaConfig {

    /**
     * Topic 名称常量
     */
    public static final String TOPIC_LAB_ENV_DATA = "lab-environment-data";        // 实验室环境数据
    public static final String TOPIC_LAB_ATTENDANCE = "lab-attendance-data";      // 实验室考勤数据
    public static final String TOPIC_LAB_ALARM = "lab-alarm-data";                // 实验室告警数据
    public static final String TOPIC_LAB_ENV_DATA_DLQ = "lab-environment-data-dlq";  // 环境数据死信队列
    public static final String TOPIC_LAB_ALARM_DLQ = "lab-alarm-data-dlq";          // 告警死信队列

    /**
     * 创建实验室环境数据 Topic
     *
     * 配置说明:
     * - partitions(5): 5个分区，按实验室ID分区，保证同一实验室数据有序
     * - replicas(1): 1个副本（单机环境，生产环境建议3个）
     */
    @Bean
    public NewTopic labEnvironmentDataTopic() {
        return TopicBuilder
                .name(TOPIC_LAB_ENV_DATA)
                .partitions(5)           // 5个分区，按实验室ID取模
                .replicas(1)             // 1个副本
                .build();
    }

    /**
     * 创建实验室考勤数据 Topic
     *
     * 配置说明:
     * - partitions(3): 3个分区，考勤数据相对较少
     * - replicas(1): 1个副本
     */
    @Bean
    public NewTopic labAttendanceDataTopic() {
        return TopicBuilder
                .name(TOPIC_LAB_ATTENDANCE)
                .partitions(3)           // 3个分区
                .replicas(1)             // 1个副本
                .build();
    }

    /**
     * 创建实验室告警数据 Topic
     *
     * 配置说明:
     * - partitions(2): 2个分区，告警数据量不大
     * - replicas(1): 1个副本
     */
    @Bean
    public NewTopic labAlarmDataTopic() {
        return TopicBuilder
                .name(TOPIC_LAB_ALARM)
                .partitions(2)           // 2个分区
                .replicas(1)             // 1个副本
                .build();
    }

    /**
     * 创建实验室环境数据死信队列 Topic
     *
     * 死信队列用于存储处理失败的环境数据消息，便于后续分析和重新处理
     */
    @Bean
    public NewTopic labEnvironmentDataDlqTopic() {
        return TopicBuilder
                .name(TOPIC_LAB_ENV_DATA_DLQ)
                .partitions(1)           // 死信队列一般不需要太多分区
                .replicas(1)             // 1个副本
                .build();
    }

    /**
     * 创建实验室告警死信队列 Topic
     */
    @Bean
    public NewTopic labAlarmDlqTopic() {
        return TopicBuilder
                .name(TOPIC_LAB_ALARM_DLQ)
                .partitions(1)           // 死信队列一般不需要太多分区
                .replicas(1)             // 1个副本
                .build();
    }
}
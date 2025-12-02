package com.sewage.monitor.controller;

import com.sewage.common.result.Result;
import com.sewage.monitor.kafka.producer.LabEnvironmentProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka测试控制器
 * 用于测试Kafka功能是否正常工作
 */
@Slf4j
@RestController
@RequestMapping("/kafka-test")
@RequiredArgsConstructor
public class KafkaTestController {

    private final LabEnvironmentProducer labEnvironmentProducer;

    /**
     * 测试发送简单文本消息
     *
     * GET /kafka-test/send-text?message=hello
     */
    @GetMapping("/send-text")
    public Result<String> sendTextMessage(@RequestParam(defaultValue = "Hello Kafka!") String message) {
        try {
            // 使用环境数据生产者发送测试消息（临时方案）
            Map<String, Object> testData = new HashMap<>();
            testData.put("type", "TEST");
            testData.put("message", message);
            testData.put("timestamp", System.currentTimeMillis());

            // 这里我们简单创建一个JSON字符串
            String jsonMessage = "{\"type\":\"TEST\",\"message\":\"" + message + "\",\"timestamp\":" + System.currentTimeMillis() + "}";

            // 通过生产者发送（模拟环境数据格式）
            labEnvironmentProducer.getKafkaTemplate().send("lab-environment-data", "test-key", jsonMessage);

            log.info("🧪 [Kafka测试] 已发送文本消息: {}", message);
            return Result.success("测试消息发送成功: " + message);

        } catch (Exception e) {
            log.error("❌ [Kafka测试] 文本消息发送失败: {}", e.getMessage(), e);
            return Result.failure("测试消息发送失败: " + e.getMessage());
        }
    }

    /**
     * 测试Kafka连接状态
     *
     * GET /kafka-test/status
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> checkKafkaStatus() {
        Map<String, Object> status = new HashMap<>();

        try {
            // 简单的状态检查
            status.put("kafka", "UP");
            status.put("producer", labEnvironmentProducer != null ? "UP" : "DOWN");
            status.put("timestamp", System.currentTimeMillis());
            status.put("message", "Kafka连接正常");

            log.info("🧪 [Kafka测试] 状态检查完成 - 生产者状态: {}", status.get("producer"));
            return Result.success(status);

        } catch (Exception e) {
            status.put("kafka", "DOWN");
            status.put("error", e.getMessage());
            status.put("timestamp", System.currentTimeMillis());

            log.error("❌ [Kafka测试] 状态检查失败: {}", e.getMessage(), e);
            return Result.failure("Kafka状态检查失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     *
     * GET /kafka-test/health
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("Kafka测试服务运行正常");
    }
}
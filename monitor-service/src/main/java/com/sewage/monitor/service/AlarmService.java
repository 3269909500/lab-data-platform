package com.sewage.monitor.service;

import com.sewage.monitor.entity.LabAlarm;
import com.sewage.monitor.entity.LabEnvironmentData;
import com.sewage.monitor.kafka.producer.LabAlarmProducer;  // 恢复Kafka
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 实验室告警服务 - 临时禁用
 * 负责检查实验室环境数据并生成告警
 */
@Slf4j
@Service  // 恢复AlarmService
@RequiredArgsConstructor
public class AlarmService {

    private final LabAlarmProducer labAlarmProducer;  // 恢复Kafka
    private final WebSocketPushService webSocketPushService;

    // 告警阈值配置（可以移动到配置文件中）
    private static final double TEMP_MIN = 18.0;      // 最低温度 18°C
    private static final double TEMP_MAX = 28.0;      // 最高温度 28°C
    private static final double TEMP_CRITICAL_MIN = 10.0; // 严重低温 10°C
    private static final double TEMP_CRITICAL_MAX = 35.0; // 严重高温 35°C

    private static final double HUMIDITY_MIN = 40.0;  // 最低湿度 40%
    private static final double HUMIDITY_MAX = 70.0;  // 最高湿度 70%
    private static final double HUMIDITY_CRITICAL_MIN = 20.0; // 严重低湿 20%
    private static final double HUMIDITY_CRITICAL_MAX = 90.0; // 严重高湿 90%

    private static final double PM25_THRESHOLD = 75.0; // PM2.5标准 75μg/m³
    private static final double PM25_DANGER = 150.0;   // PM2.5危险值 150μg/m³

    private static final double CO2_THRESHOLD = 1000.0; // CO2标准 1000ppm
    private static final double CO2_DANGER = 2000.0;   // CO2危险值 2000ppm

    private static final double ILLUMINANCE_MIN = 300.0; // 最低照度 300lux

    /**
     * 检查实验室环境数据并发送告警
     */
    public void checkAndSendAlarm(LabEnvironmentData data) {
        try {
            log.info("开始检查实验室环境数据告警 - 实验室ID: {}, 实验室: {}", data.getLabId(), data.getLabName());

            // 温度告警检查
            if (data.getTemperature() != null) {
                if (data.getTemperature() < TEMP_CRITICAL_MIN || data.getTemperature() > TEMP_CRITICAL_MAX) {
                    sendAlarm(data, "TEMP_HIGH", "DANGER",
                            String.format("温度严重异常: %.1f°C", data.getTemperature()),
                            data.getTemperature(), null);
                } else if (data.getTemperature() < TEMP_MIN || data.getTemperature() > TEMP_MAX) {
                    sendAlarm(data, "TEMP_HIGH", "WARNING",
                            String.format("温度超出正常范围: %.1f°C", data.getTemperature()),
                            data.getTemperature(), null);
                }
            }

            // 湿度告警检查
            if (data.getHumidity() != null) {
                if (data.getHumidity() < HUMIDITY_CRITICAL_MIN || data.getHumidity() > HUMIDITY_CRITICAL_MAX) {
                    sendAlarm(data, "HUMIDITY_HIGH", "DANGER",
                            String.format("湿度严重异常: %.1f%%", data.getHumidity()),
                            data.getHumidity(), null);
                } else if (data.getHumidity() < HUMIDITY_MIN || data.getHumidity() > HUMIDITY_MAX) {
                    sendAlarm(data, "HUMIDITY_HIGH", "WARNING",
                            String.format("湿度超出正常范围: %.1f%%", data.getHumidity()),
                            data.getHumidity(), null);
                }
            }

            // PM2.5告警检查
            if (data.getPm25() != null) {
                if (data.getPm25() > PM25_DANGER) {
                    sendAlarm(data, "PM25_HIGH", "DANGER",
                            String.format("PM2.5严重超标: %.1f μg/m³", data.getPm25()),
                            data.getPm25(), PM25_DANGER);
                } else if (data.getPm25() > PM25_THRESHOLD) {
                    sendAlarm(data, "PM25_HIGH", "WARNING",
                            String.format("PM2.5超标: %.1f μg/m³", data.getPm25()),
                            data.getPm25(), PM25_THRESHOLD);
                }
            }

            // CO2告警检查
            if (data.getCo2() != null) {
                if (data.getCo2() > CO2_DANGER) {
                    sendAlarm(data, "CO2_HIGH", "DANGER",
                            String.format("CO2浓度严重过高: %.1f ppm", data.getCo2()),
                            data.getCo2(), CO2_DANGER);
                } else if (data.getCo2() > CO2_THRESHOLD) {
                    sendAlarm(data, "CO2_HIGH", "WARNING",
                            String.format("CO2浓度过高: %.1f ppm", data.getCo2()),
                            data.getCo2(), CO2_THRESHOLD);
                }
            }

            // 照度告警检查
            if (data.getIlluminance() != null && data.getIlluminance() < ILLUMINANCE_MIN) {
                sendAlarm(data, "ILLUMINANCE_LOW", "WARNING",
                        String.format("照度过低: %.1f lux", data.getIlluminance()),
                        data.getIlluminance(), ILLUMINANCE_MIN);
            }

            // 人数告警检查（假设容量为30人）
            if (data.getCurrentPeopleCount() != null && data.getCurrentPeopleCount() > 30) {
                sendAlarm(data, "PEOPLE_EXCEED", "WARNING",
                        String.format("人数超载: %d人", data.getCurrentPeopleCount()),
                        (double) data.getCurrentPeopleCount(), 30.0);
            }

            log.info("告警检查完成 - 实验室: {}", data.getLabName());

        } catch (Exception e) {
            log.error("❌ 实验室告警检查失败: {}", e.getMessage(), e);
        }
    }

    private void sendAlarm(LabEnvironmentData data, String alarmType, String alarmLevel,
                          String alarmMessage, Double currentValue, Double thresholdValue) {
        try {
            // 创建告警对象
            LabAlarm alarm = new LabAlarm();
            alarm.setLabId(data.getLabId());
            alarm.setLabName(data.getLabName());

            // 将String类型的告警类型转换为枚举
            LabAlarm.AlarmType typeEnum = LabAlarm.AlarmType.valueOf(alarmType);
            alarm.setAlarmType(typeEnum.getCode());

            // 将String类型的告警级别转换为枚举
            LabAlarm.AlarmLevel levelEnum = LabAlarm.AlarmLevel.valueOf(alarmLevel);
            alarm.setAlarmLevel(levelEnum.getCode());

            alarm.setAlarmMessage(alarmMessage);
            alarm.setAlarmValue(currentValue != null ? currentValue : 0.0);
            alarm.setThresholdValue(thresholdValue != null ? thresholdValue : 0.0);
            alarm.setAlarmTime(LocalDateTime.now());
            alarm.setStatus(LabAlarm.HandleStatus.PENDING);

            // ✅ 恢复Kafka功能：发送告警消息
            labAlarmProducer.sendAlarm(alarm);

            // ✅ WebSocket推送告警信息
            webSocketPushService.pushAlarm(alarm);

            log.info("🚨 告警已发送到Kafka和WebSocket - 类型: {}, 级别: {}, 实验室: {}, 消息: {}",
                    alarmType, alarmLevel, data.getLabName(), alarmMessage);
        } catch (Exception e) {
            log.error("❌ 告警发送失败: {}", e.getMessage(), e);
        }
    }
}
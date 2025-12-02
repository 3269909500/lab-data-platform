package com.sewage.monitor.service;

import com.sewage.monitor.entity.LabEnvironmentData;
import com.sewage.monitor.entity.LabAlarm;
import com.sewage.monitor.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * WebSocket推送服务
 * 负责将各种数据通过WebSocket实时推送给前端
 *
 * @author system
 */
@Slf4j
@Service
public class WebSocketPushService {

    /**
     * 推送环境数据
     */
    public void pushEnvironmentData(LabEnvironmentData data) {
        try {
            if (data != null && data.getLabId() != null) {
                WebSocketServer.pushEnvironmentData(data);
                log.debug("📡 WebSocket环境数据推送成功 - 实验室ID: {}, 时间: {}",
                    data.getLabId(), data.getMonitorTime());
            }
        } catch (Exception e) {
            log.error("❌ WebSocket环境数据推送失败", e);
        }
    }

    /**
     * 推送告警信息
     */
    public void pushAlarm(LabAlarm alarm) {
        try {
            if (alarm != null && alarm.getLabId() != null) {
                WebSocketServer.pushAlarm(alarm);
                log.info("🚨 WebSocket告警信息推送成功 - 实验室ID: {}, 告警类型: {}",
                    alarm.getLabId(), alarm.getAlarmType());
            }
        } catch (Exception e) {
            log.error("❌ WebSocket告警信息推送失败", e);
        }
    }

    /**
     * 推送统计数据
     */
    public void pushStatistics(Long labId, Object statistics) {
        try {
            if (labId != null && statistics != null) {
                WebSocketServer.pushStatistics(labId, statistics);
                log.debug("📊 WebSocket统计数据推送成功 - 实验室ID: {}", labId);
            }
        } catch (Exception e) {
            log.error("❌ WebSocket统计数据推送失败", e);
        }
    }

    /**
     * 获取连接统计信息
     */
    public Map<String, Object> getConnectionStats() {
        try {
            String statsJson = WebSocketServer.getConnectionStats();
            return Map.of(
                "totalOnlineCount", WebSocketServer.getOnlineCount(),
                "labConnectionStats", statsJson
            );
        } catch (Exception e) {
            log.error("❌ 获取WebSocket连接统计失败", e);
            return Map.of("totalOnlineCount", 0, "labConnectionStats", "{}");
        }
    }

    /**
     * 检查指定实验室的WebSocket连接状态
     */
    public boolean isLabConnected(Long labId) {
        return WebSocketServer.getLabOnlineCount(labId) > 0;
    }

    /**
     * 获取指定实验室的连接数
     */
    public int getLabConnectionCount(Long labId) {
        return WebSocketServer.getLabOnlineCount(labId);
    }

    /**
     * 广播消息到所有连接的客户端
     * 用于推送系统级通知，如定时生成的报表
     *
     * @param message 消息内容
     */
    public void pushToAll(Object message) {
        try {
            WebSocketServer.broadcastToAll(message);
            log.info("📢 广播消息已发送到所有客户端");
        } catch (Exception e) {
            log.error("❌ 广播消息失败", e);
        }
    }
}
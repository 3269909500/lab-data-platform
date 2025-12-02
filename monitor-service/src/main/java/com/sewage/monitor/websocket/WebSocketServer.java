package com.sewage.monitor.websocket;

import com.alibaba.fastjson.JSON;
import com.sewage.common.context.UserContext;
import com.sewage.monitor.entity.LabEnvironmentData;
import com.sewage.monitor.entity.LabAlarm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket服务端
 * 实现实时数据推送功能
 *
 * @author system
 */
@Slf4j
@Component
@ServerEndpoint("/ws/realtime/{labId}")
public class WebSocketServer {

    /**
     * 静态变量，用来记录当前在线连接数
     */
    private static int onlineCount = 0;

    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的WebSocket对象。
     */
    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();

    /**
     * 与客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;

    /**
     * 实验室ID
     */
    private Long labId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("labId") Long labId) {
        this.session = session;
        this.labId = labId;
        webSocketSet.add(this);
        addOnlineCount();

        // 从session中获取用户信息
        String userIdHeader = session.getUserProperties().get("X-User-Id") != null ?
            session.getUserProperties().get("X-User-Id").toString() : null;
        if (userIdHeader != null) {
            this.userId = Long.parseLong(userIdHeader);
        }

        log.info("有新连接加入！当前在线人数为：{}, 实验室ID：{}, 用户ID：{}", getOnlineCount(), labId, userId);

        // 发送连接成功消息
        try {
            sendMessage(JSON.toJSONString(createMessage("CONNECTION", "连接成功", labId, null)));
        } catch (IOException e) {
            log.error("WebSocket发送消息失败", e);
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);
        subOnlineCount();
        log.info("有一连接关闭！当前在线人数为：{}, 实验室ID：{}, 用户ID：{}", getOnlineCount(), labId, userId);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("来自客户端的消息：{}, 实验室ID：{}", message, labId);

        // 可以处理客户端发送的命令，比如订阅特定类型的数据
        try {
            // 这里可以添加对客户端命令的处理逻辑
            // 例如：订阅/取消订阅某些数据类型
        } catch (Exception e) {
            log.error("处理客户端消息失败", e);
        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket发生错误，实验室ID：{}, 用户ID：{}", labId, userId, error);
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 群发自定义消息
     */
    public static void sendInfo(Long labId, String message) {
        for (WebSocketServer item : webSocketSet) {
            // 只向订阅了该实验室的客户端推送数据
            if (item.labId.equals(labId)) {
                try {
                    item.sendMessage(message);
                    log.info("向实验室{}的客户端推送消息成功：{}", labId, message);
                } catch (IOException e) {
                    log.error("向实验室{}的客户端推送消息失败：{}", labId, message, e);
                }
            }
        }
    }

    /**
     * 推送环境数据
     */
    public static void pushEnvironmentData(LabEnvironmentData data) {
        String message = JSON.toJSONString(createMessage("ENVIRONMENT_DATA", "环境数据更新", data.getLabId(), data));
        sendInfo(data.getLabId(), message);
    }

    /**
     * 推送告警信息
     */
    public static void pushAlarm(LabAlarm alarm) {
        String message = JSON.toJSONString(createMessage("ALARM", "新告警", alarm.getLabId(), alarm));
        sendInfo(alarm.getLabId(), message);
    }

    /**
     * 推送统计数据
     */
    public static void pushStatistics(Long labId, Object statistics) {
        String message = JSON.toJSONString(createMessage("STATISTICS", "统计数据更新", labId, statistics));
        sendInfo(labId, message);
    }

    /**
     * 创建统一格式的消息
     */
    private static Object createMessage(String type, String message, Long labId, Object data) {
        ConcurrentHashMap<String, Object> msgObj = new ConcurrentHashMap<>();
        msgObj.put("type", type);
        msgObj.put("message", message);
        msgObj.put("labId", labId);
        msgObj.put("timestamp", System.currentTimeMillis());
        msgObj.put("data", data);
        return msgObj;
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }

    /**
     * 获取当前实验室的在线连接数
     */
    public static synchronized int getLabOnlineCount(Long labId) {
        return (int) webSocketSet.stream()
                .filter(item -> item.labId.equals(labId))
                .count();
    }

    /**
     * 获取所有实验室的在线连接数统计
     */
    public static String getConnectionStats() {
        ConcurrentHashMap<Long, Integer> labStats = new ConcurrentHashMap<>();
        for (WebSocketServer item : webSocketSet) {
            labStats.put(item.labId, labStats.getOrDefault(item.labId, 0) + 1);
        }
        return JSON.toJSONString(labStats);
    }

    /**
     * 广播消息到所有连接的客户端
     *
     * @param message 消息对象
     */
    public static void broadcastToAll(Object message) {
        String jsonMessage = JSON.toJSONString(message);
        log.info("📢 开始广播消息到所有客户端 - 当前在线: {} 人", webSocketSet.size());

        int successCount = 0;
        int failCount = 0;

        for (WebSocketServer item : webSocketSet) {
            try {
                item.sendMessage(jsonMessage);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("❌ 广播消息失败 - 实验室ID: {}", item.labId, e);
            }
        }

        log.info("✅ 广播完成 - 成功: {}, 失败: {}", successCount, failCount);
    }
}
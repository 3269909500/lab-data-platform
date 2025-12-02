package com.sewage.monitor.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 异步任务管理器
 * 功能：管理Excel报表生成等异步任务的状态和结果
 */
@Slf4j
@Service
public class AsyncTaskManager {

    // 任务存储（实际生产环境应该使用Redis）
    private final Map<String, AsyncTask> tasks = new ConcurrentHashMap<>();

    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        PENDING("待处理"),
        PROCESSING("处理中"),
        COMPLETED("已完成"),
        FAILED("失败");

        private final String desc;

        TaskStatus(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 异步任务实体
     */
    @Data
    @AllArgsConstructor
    public static class AsyncTask {
        private String taskId;
        private String taskType;
        private TaskStatus status;
        private Integer progress;  // 进度百分比 0-100
        private String message;
        private Object result;     // 任务结果（如文件路径）
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private LocalDateTime completeTime;

        public AsyncTask(String taskId, String taskType) {
            this.taskId = taskId;
            this.taskType = taskType;
            this.status = TaskStatus.PENDING;
            this.progress = 0;
            this.message = "任务已创建";
            this.createTime = LocalDateTime.now();
            this.updateTime = LocalDateTime.now();
        }
    }

    /**
     * 创建新任务
     *
     * @param taskType 任务类型
     * @return 任务ID
     */
    public String createTask(String taskType) {
        String taskId = UUID.randomUUID().toString();
        AsyncTask task = new AsyncTask(taskId, taskType);
        tasks.put(taskId, task);

        log.info("📝 创建异步任务 - ID: {}, 类型: {}", taskId, taskType);
        return taskId;
    }

    /**
     * 更新任务状态
     *
     * @param taskId 任务ID
     * @param status 状态
     * @param progress 进度
     * @param message 消息
     */
    public void updateTask(String taskId, TaskStatus status, Integer progress, String message) {
        AsyncTask task = tasks.get(taskId);
        if (task == null) {
            log.warn("任务不存在: {}", taskId);
            return;
        }

        task.setStatus(status);
        task.setProgress(progress);
        task.setMessage(message);
        task.setUpdateTime(LocalDateTime.now());

        if (status == TaskStatus.COMPLETED || status == TaskStatus.FAILED) {
            task.setCompleteTime(LocalDateTime.now());
        }

        log.info("📊 更新任务状态 - ID: {}, 状态: {}, 进度: {}%, 消息: {}",
                taskId, status.getDesc(), progress, message);
    }

    /**
     * 设置任务结果
     *
     * @param taskId 任务ID
     * @param result 结果对象
     */
    public void setTaskResult(String taskId, Object result) {
        AsyncTask task = tasks.get(taskId);
        if (task == null) {
            log.warn("任务不存在: {}", taskId);
            return;
        }

        task.setResult(result);
        task.setUpdateTime(LocalDateTime.now());
        log.info("💾 设置任务结果 - ID: {}", taskId);
    }

    /**
     * 标记任务完成
     *
     * @param taskId 任务ID
     * @param result 结果对象
     */
    public void completeTask(String taskId, Object result) {
        updateTask(taskId, TaskStatus.COMPLETED, 100, "任务完成");
        setTaskResult(taskId, result);
    }

    /**
     * 标记任务失败
     *
     * @param taskId 任务ID
     * @param errorMessage 错误消息
     */
    public void failTask(String taskId, String errorMessage) {
        updateTask(taskId, TaskStatus.FAILED, 0, errorMessage);
        log.error("❌ 任务失败 - ID: {}, 错误: {}", taskId, errorMessage);
    }

    /**
     * 获取任务信息
     *
     * @param taskId 任务ID
     * @return 任务对象
     */
    public AsyncTask getTask(String taskId) {
        return tasks.get(taskId);
    }

    /**
     * 删除任务
     *
     * @param taskId 任务ID
     */
    public void removeTask(String taskId) {
        tasks.remove(taskId);
        log.info("🗑️ 删除任务 - ID: {}", taskId);
    }

    /**
     * 清理过期任务（完成超过24小时的）
     */
    public void cleanExpiredTasks() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        int removed = 0;

        for (Map.Entry<String, AsyncTask> entry : tasks.entrySet()) {
            AsyncTask task = entry.getValue();
            if (task.getCompleteTime() != null && task.getCompleteTime().isBefore(threshold)) {
                tasks.remove(entry.getKey());
                removed++;
            }
        }

        if (removed > 0) {
            log.info("🧹 清理过期任务 - 删除数量: {}", removed);
        }
    }

    /**
     * 获取所有任务
     */
    public Map<String, AsyncTask> getAllTasks() {
        return new ConcurrentHashMap<>(tasks);
    }
}

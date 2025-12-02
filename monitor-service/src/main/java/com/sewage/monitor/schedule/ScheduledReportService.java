package com.sewage.monitor.schedule;

import com.sewage.monitor.service.AsyncTaskManager;
import com.sewage.monitor.service.ReportExportService;
import com.sewage.monitor.service.WebSocketPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 定时任务调度服务
 * 功能：凌晨1点自动生成统计报表并推送给前端
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledReportService {

    private final ReportExportService reportExportService;
    private final AsyncTaskManager asyncTaskManager;
    private final WebSocketPushService webSocketPushService;

    /**
     * 每天凌晨1点生成日统计报表
     * cron表达式: 秒 分 时 日 月 周
     * 0 0 1 * * ? = 每天凌晨1点
     *
     * 测试用：每5分钟执行一次
     * 0 *​/5 * * * ? = 每5分钟
     */
    @Scheduled(cron = "0 0 1 * * ?")  // 凌晨1点执行
    // @Scheduled(cron = "0 */5 * * * ?")  // 测试用：每5分钟
    public void generateDailyReport() {
        log.info("🕐 定时任务触发 - 开始生成昨日统计报表");

        try {
            // 计算昨天的日期
            LocalDate yesterday = LocalDate.now().minusDays(1);
            LocalDate startDate = yesterday;
            LocalDate endDate = yesterday;

            // 创建异步任务
            String taskId = asyncTaskManager.createTask("DAILY_REPORT");
            log.info("📝 创建报表生成任务 - ID: {}", taskId);

            // 异步执行报表生成
            generateReportAsync(taskId, startDate, endDate);

        } catch (Exception e) {
            log.error("❌ 定时任务执行失败", e);
        }
    }

    /**
     * 异步生成报表
     *
     * @param taskId 任务ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     */
    @Async("reportExecutor")
    public void generateReportAsync(String taskId, LocalDate startDate, LocalDate endDate) {
        try {
            // 更新任务状态
            asyncTaskManager.updateTask(taskId, AsyncTaskManager.TaskStatus.PROCESSING, 10, "开始生成报表");

            // 生成Excel文件
            log.info("📊 开始生成Excel报表 - 日期范围: {} 到 {}", startDate, endDate);
            byte[] excelBytes = reportExportService.generateDailyStatisticsReport(startDate, endDate);

            asyncTaskManager.updateTask(taskId, AsyncTaskManager.TaskStatus.PROCESSING, 60, "Excel生成完成");

            // 生成文件名
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            String fileName = String.format("日统计报表_%s_%s.xlsx",
                    startDate.format(formatter),
                    endDate.format(formatter));

            // 保存文件到文件系统
            log.info("💾 保存报表文件 - 文件名: {}", fileName);
            String filePath = reportExportService.saveReportToFile(excelBytes, fileName);

            asyncTaskManager.updateTask(taskId, AsyncTaskManager.TaskStatus.PROCESSING, 90, "报表保存完成");

            // 完成任务
            Map<String, Object> result = new HashMap<>();
            result.put("fileName", fileName);
            result.put("filePath", filePath);
            result.put("fileSize", excelBytes.length);
            result.put("date", startDate.toString());

            asyncTaskManager.completeTask(taskId, result);

            log.info("✅ 报表生成成功 - 任务ID: {}, 文件: {}, 大小: {} KB",
                    taskId, fileName, excelBytes.length / 1024);

            // 推送通知到所有连接的前端
            pushReportNotification(taskId, fileName, filePath, excelBytes.length);

        } catch (Exception e) {
            log.error("❌ 报表生成失败 - 任务ID: {}", taskId, e);
            asyncTaskManager.failTask(taskId, "报表生成失败: " + e.getMessage());

            // 推送失败通知
            pushFailureNotification(taskId, e.getMessage());
        }
    }

    /**
     * 推送报表生成成功通知
     */
    private void pushReportNotification(String taskId, String fileName, String filePath, long fileSize) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "REPORT_READY");
            notification.put("taskId", taskId);
            notification.put("fileName", fileName);
            notification.put("filePath", filePath);
            notification.put("fileSize", fileSize);
            notification.put("fileSizeKB", fileSize / 1024);
            notification.put("message", "日统计报表已生成");
            notification.put("timestamp", System.currentTimeMillis());

            // 推送到所有在线的WebSocket连接
            // 注意：这里推送到实验室ID=0，表示广播给所有连接
            webSocketPushService.pushToAll(notification);

            log.info("📤 报表通知已推送 - 任务ID: {}", taskId);

        } catch (Exception e) {
            log.error("❌ 推送报表通知失败", e);
        }
    }

    /**
     * 推送报表生成失败通知
     */
    private void pushFailureNotification(String taskId, String errorMessage) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "REPORT_FAILED");
            notification.put("taskId", taskId);
            notification.put("message", "报表生成失败");
            notification.put("error", errorMessage);
            notification.put("timestamp", System.currentTimeMillis());

            webSocketPushService.pushToAll(notification);

            log.info("📤 失败通知已推送 - 任务ID: {}", taskId);

        } catch (Exception e) {
            log.error("❌ 推送失败通知失败", e);
        }
    }

    /**
     * 清理过期任务（每天凌晨2点执行）
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredTasks() {
        log.info("🧹 定时任务触发 - 清理过期任务");
        asyncTaskManager.cleanExpiredTasks();
    }
}

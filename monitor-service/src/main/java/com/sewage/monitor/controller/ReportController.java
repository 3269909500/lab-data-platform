package com.sewage.monitor.controller;

import com.sewage.common.result.Result;
import com.sewage.monitor.schedule.ScheduledReportService;
import com.sewage.monitor.service.AsyncTaskManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 报表管理控制器
 * 提供报表生成、查询、下载等功能
 */
@Slf4j
@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final ScheduledReportService scheduledReportService;
    private final AsyncTaskManager asyncTaskManager;

    /**
     * 手动触发报表生成
     *
     * POST http://localhost:8083/report/generate
     * Body: { "startDate": "2025-11-26", "endDate": "2025-11-27" }
     */
    @PostMapping("/generate")
    public Result<Map<String, String>> generateReport(@RequestBody Map<String, String> request) {
        try {
            String startDateStr = request.get("startDate");
            String endDateStr = request.get("endDate");

            // 解析日期
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startDate = LocalDate.parse(startDateStr, formatter);
            LocalDate endDate = LocalDate.parse(endDateStr, formatter);

            log.info("📊 手动触发报表生成 - 日期范围: {} 到 {}", startDate, endDate);

            // 创建异步任务
            String taskId = asyncTaskManager.createTask("MANUAL_REPORT");

            // 异步生成报表
            scheduledReportService.generateReportAsync(taskId, startDate, endDate);

            // 返回任务ID
            Map<String, String> result = new HashMap<>();
            result.put("taskId", taskId);
            result.put("message", "报表生成任务已提交，请稍后查询任务状态");

            return Result.success(result);

        } catch (Exception e) {
            log.error("❌ 手动生成报表失败", e);
            return Result.failure("报表生成失败: " + e.getMessage());
        }
    }

    /**
     * 查询任务状态
     *
     * GET http://localhost:8083/report/task/{taskId}
     */
    @GetMapping("/task/{taskId}")
    public Result<AsyncTaskManager.AsyncTask> getTaskStatus(@PathVariable String taskId) {
        try {
            AsyncTaskManager.AsyncTask task = asyncTaskManager.getTask(taskId);

            if (task == null) {
                return Result.failure("任务不存在");
            }

            return Result.success(task);

        } catch (Exception e) {
            log.error("❌ 查询任务状态失败 - 任务ID: {}", taskId, e);
            return Result.failure("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有任务
     *
     * GET http://localhost:8083/report/tasks
     */
    @GetMapping("/tasks")
    public Result<Map<String, AsyncTaskManager.AsyncTask>> getAllTasks() {
        try {
            Map<String, AsyncTaskManager.AsyncTask> tasks = asyncTaskManager.getAllTasks();
            return Result.success(tasks);
        } catch (Exception e) {
            log.error("❌ 获取所有任务失败", e);
            return Result.failure("查询失败: " + e.getMessage());
        }
    }

    /**
     * 下载报表文件
     *
     * GET http://localhost:8083/report/download/{fileName}
     */
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadReport(@PathVariable String fileName) {
        try {
            // 构建文件路径
            String filePath = "reports" + File.separator + fileName;
            File file = new File(filePath);

            if (!file.exists()) {
                log.warn("⚠️ 报表文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // 创建资源
            Resource resource = new FileSystemResource(file);

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            log.info("📥 下载报表文件: {}", fileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (Exception e) {
            log.error("❌ 下载报表失败 - 文件名: {}", fileName, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 测试接口 - 立即生成昨日报表
     *
     * GET http://localhost:8083/report/test/generate-yesterday
     */
    @GetMapping("/test/generate-yesterday")
    public Result<Map<String, String>> testGenerateYesterday() {
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);

            log.info("🧪 测试生成昨日报表: {}", yesterday);

            // 创建异步任务
            String taskId = asyncTaskManager.createTask("TEST_REPORT");

            // 异步生成报表
            scheduledReportService.generateReportAsync(taskId, yesterday, yesterday);

            Map<String, String> result = new HashMap<>();
            result.put("taskId", taskId);
            result.put("date", yesterday.toString());
            result.put("message", "测试报表生成任务已提交");

            return Result.success(result);

        } catch (Exception e) {
            log.error("❌ 测试生成报表失败", e);
            return Result.failure("生成失败: " + e.getMessage());
        }
    }
}

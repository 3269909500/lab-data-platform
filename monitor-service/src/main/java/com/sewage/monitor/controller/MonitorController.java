package com.sewage.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sewage.common.context.UserContext;
import com.sewage.common.result.Result;
import com.sewage.monitor.entity.LabDailyStatistics;
import com.sewage.monitor.entity.LabAlarm;
import com.sewage.monitor.entity.LabEnvironmentData;
import com.sewage.monitor.kafka.producer.LabEnvironmentProducer;
import com.sewage.monitor.mapper.LabDailyStatisticsMapper;
import com.sewage.monitor.mapper.LabAlarmMapper;
import com.sewage.monitor.mapper.LabEnvironmentDataMapper;
import com.sewage.monitor.service.LabEnvironmentDataService;
import com.sewage.monitor.service.WebSocketPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 实验室监控控制器
 *
 * 改动说明:
 * 1. 水质监测改造为实验室环境监测
 * 2. uploadData() 改为发送到 Kafka，而不是直接保存
 * 3. 保留所有原有查询接口
 * 4. 新增告警和统计查询接口
 * 5. 新增测试接口
 */
@Slf4j
@RestController
@RequestMapping("/lab-monitor")
@RequiredArgsConstructor
public class MonitorController {

    // 实验室环境数据服务
    private final LabEnvironmentDataService labEnvironmentDataService;

    // Kafka生产者
    private final LabEnvironmentProducer labEnvironmentProducer;

    // Mapper依赖
    private final LabEnvironmentDataMapper labEnvironmentDataMapper;
    private final LabAlarmMapper labAlarmMapper;
    private final LabDailyStatisticsMapper dailyStatisticsMapper;

    // WebSocket推送服务
    private final WebSocketPushService webSocketPushService;

    // ========================================
    // 数据上传接口 (改造重点！)
    // ========================================

    /**
     * 上传实验室环境数据 - 临时直接保存到数据库
     *
     * 原来: 发送到 Kafka → 消费者异步处理
     * 现在: 直接保存到数据库（Kafka禁用期间）
     *
     * POST http://localhost:8083/lab-monitor/data
     */
    @PostMapping("/data")
    public Result<String> uploadData(@RequestBody LabEnvironmentData data) {
        try {
            log.info("📥 接收到实验室环境数据 - 实验室ID: {}, 名称: {}",
                    data.getLabId(), data.getLabName());

            // 设置当前时间和数据来源
            data.setMonitorTime(LocalDateTime.now());
            if (data.getDataSource() == null) {
                data.setDataSource("SENSOR");
            }

            // ✅ 恢复Kafka功能：发送到消息队列
            labEnvironmentProducer.sendEnvironmentDataAsync(data);

            log.info("✅ 实验室环境数据已发送到Kafka - 实验室: {}", data.getLabName());
            return Result.success("数据已发送到消息队列");

        } catch (Exception e) {
            log.error("❌ 数据上传失败: {}", e.getMessage(), e);
            return Result.failure("数据上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传实验室环境数据 (新增)
     *
     * POST http://localhost:8083/lab-monitor/data/batch
     */
    @PostMapping("/data/batch")
    public Result<String> uploadBatchData(@RequestBody List<LabEnvironmentData> dataList) {
        try {
            log.info("📦 批量接收实验室环境数据 - 数量: {}", dataList.size());

            // 设置数据并直接保存到数据库
            for (LabEnvironmentData data : dataList) {
                if (data.getMonitorTime() == null) {
                    data.setMonitorTime(LocalDateTime.now());
                }
                if (data.getDataSource() == null) {
                    data.setDataSource("SENSOR");
                }

                // 发送到Kafka
                labEnvironmentProducer.sendEnvironmentDataAsync(data);
            }

            return Result.success("已发送 " + dataList.size() + " 条实验室环境数据到消息队列");

        } catch (Exception e) {
            log.error("❌ 批量上传失败: {}", e.getMessage(), e);
            return Result.failure("批量上传失败: " + e.getMessage());
        }
    }

    // ========================================
    // 原有查询接口 (保持不变)
    // ========================================

    /**
     * 获取最新监测数据
     *
     * GET http://localhost:8083/monitor/latest/1
     */
    @GetMapping("/latest/{plantId}")
    public Result<LabEnvironmentData> getLatestData(@PathVariable Long plantId) {
        LabEnvironmentData data = labEnvironmentDataService.getLatestData(plantId);
        return Result.success(data);
    }

    /**
     * 查询历史数据
     *
     * GET http://localhost:8083/monitor/history/1?startTime=2025-01-01 00:00:00&endTime=2025-12-31 23:59:59
     */
    @GetMapping("/history/{plantId}")
    public Result<List<LabEnvironmentData>> getHistoryData(
            @PathVariable Long plantId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        List<LabEnvironmentData> list = labEnvironmentDataService.getHistoryData(plantId, startTime, endTime);
        return Result.success(list);
    }

    /**
     * 分页查询
     *
     * GET http://localhost:8083/monitor/page?plantId=1&current=1&size=10
     */
    @GetMapping("/page")
    public Result<Page<LabEnvironmentData>> getPageData(
            @RequestParam(required = false) Long plantId,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        Page<LabEnvironmentData> page = labEnvironmentDataService.getPageData(plantId, current, size);
        return Result.success(page);
    }

    // ========================================
    // 新增：告警查询接口
    // ========================================

    /**
     * 查询所有告警 (分页)
     *
     * GET http://localhost:8083/monitor/alarms/list?current=1&size=20
     */
    @GetMapping("/alarms/list")
    public Result<Page<LabAlarm>> getAllAlarms(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size) {
        try {
            log.info("📋 查询告警列表 - 页码: {}, 每页数量: {}", current, size);

            Page<LabAlarm> page = new Page<>(current, size);
            Page<LabAlarm> result = labAlarmMapper.selectPage(page, null);

            log.info("✅ 查询成功 - 总记录数: {}, 当前页记录数: {}", result.getTotal(), result.getRecords().size());
            return Result.success(result);
        } catch (Exception e) {
            log.error("❌ 查询告警列表失败", e);
            return Result.failure("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询未处理的告警
     *
     * GET http://localhost:8083/monitor/alarms/unhandled
     */
    @GetMapping("/alarms/unhandled")
    public Result<List<LabAlarm>> getUnhandledAlarms() {
        List<LabAlarm> list = labAlarmMapper.selectUnhandledAlarms();
        return Result.success(list);
    }

    /**
     * 查询某个监测点的告警
     *
     * GET http://localhost:8083/monitor/alarms/station/1
     */
    @GetMapping("/alarms/station/{stationId}")
    public Result<List<LabAlarm>> getAlarmsByStation(@PathVariable Long stationId) {
        List<LabAlarm> list = labAlarmMapper.selectByStationId(stationId, 50);
        return Result.success(list);
    }

    /**
     * 告警统计
     *
     * GET http://localhost:8083/monitor/alarms/stats
     */
    @GetMapping("/alarms/stats")
    public Result<Map<String, Object>> getAlarmStats() {
        Map<String, Object> stats = new HashMap<>();

        // 今日告警数
        Integer todayCount = labAlarmMapper.countByDate(LocalDateTime.now());
        stats.put("todayCount", todayCount);

        // 未处理告警数
        List<LabAlarm> unhandled = labAlarmMapper.selectUnhandledAlarms();
        stats.put("unhandledCount", unhandled.size());

        return Result.success(stats);
    }

    // ========================================
    // 新增：统计查询接口
    // ========================================

    /**
     * 查询今日统计
     *
     * GET http://localhost:8083/monitor/stats/today
     */
    @GetMapping("/stats/today")
    public Result<List<LabDailyStatistics>> getTodayStats() {
        List<LabDailyStatistics> list = dailyStatisticsMapper.selectByDate(LocalDate.now());
        return Result.success(list);
    }

    /**
     * 查询某个实验室的统计（最近N天）
     *
     * GET http://localhost:8083/lab-monitor/stats/lab/1?days=7
     */
    @GetMapping("/stats/lab/{labId}")
    public Result<List<LabDailyStatistics>> getStatsByLab(
            @PathVariable Long labId,
            @RequestParam(defaultValue = "7") Integer days) {
        List<LabDailyStatistics> list = dailyStatisticsMapper.selectByLabId(labId, days);
        return Result.success(list);
    }

    /**
     * 查询统计数据（分页）
     *
     * GET http://localhost:8083/monitor/stats/list?current=1&size=10
     */
    @GetMapping("/stats/list")
    public Result<Page<LabDailyStatistics>> getAllStats(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {

        Page<LabDailyStatistics> page = new Page<>(current, size);
        Page<LabDailyStatistics> result = dailyStatisticsMapper.selectPage(page,
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<LabDailyStatistics>()
                        .orderByDesc(LabDailyStatistics::getStatDate)
        );

        return Result.success(result);
    }

    // ========================================
    // 测试接口
    // ========================================

    /**
     * 测试接口
     *
     * GET http://localhost:8083/monitor/test
     */
    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("✅ 监控服务正在运行");
    }

    /**
     * 测试用户上下文（拦截器）
     *
     * GET http://localhost:8083/monitor/test-user
     * Header: Authorization: Bearer {token}
     */
    @GetMapping("/test-user")
    public Result<Map<String, Object>> testUserContext() {
        Map<String, Object> result = new HashMap<>();

        // 从ThreadLocal获取用户信息
        UserContext.UserInfo userInfo = UserContext.getUser();

        if (userInfo != null) {
            result.put("userId", userInfo.getUserId());
            result.put("username", userInfo.getUsername());
            result.put("role", userInfo.getRole());
            result.put("message", "✅ 拦截器工作正常！");
        } else {
            result.put("message", "⚠️ 未获取到用户信息，请检查请求头");
        }

        return Result.success(result);
    }

    /**
     * 测试发送正常数据
     *
     * GET http://localhost:8083/lab-monitor/test-send-normal
     */
    @GetMapping("/test-send-normal")
    public Result<String> testSendNormal() {
        LabEnvironmentData data = LabEnvironmentData.builder()
                .labId(1L)
                .labName("计算机基础实验室")
                .temperature(22.5)
                .humidity(55.0)
                .pm25(35.0)
                .illuminance(500.0)
                .co2(650.0)
                .onlineDeviceCount(15)
                .totalDeviceCount(20)
                .currentPeopleCount(25)
                .monitorTime(LocalDateTime.now())
                .dataSource("SENSOR")
                .build();

        // 发送到Kafka
        labEnvironmentProducer.sendEnvironmentDataAsync(data);
        return Result.success("✅ 已发送1条正常数据到消息队列");
    }

    /**
     * 测试发送告警数据
     *
     * GET http://localhost:8083/lab-monitor/test-send-alarm
     */
    @GetMapping("/test-send-alarm")
    public Result<String> testSendAlarm() {
        LabEnvironmentData data = LabEnvironmentData.builder()
                .labId(2L)
                .labName("化学分析实验室")
                .temperature(35.5)  // 温度过高告警！
                .humidity(85.0)     // 湿度过高告警！
                .pm25(150.0)        // PM2.5超标告警！
                .illuminance(200.0)
                .co2(1500.0)        // CO2过高告警！
                .onlineDeviceCount(5)
                .totalDeviceCount(20)
                .currentPeopleCount(45)
                .monitorTime(LocalDateTime.now())
                .dataSource("SENSOR")
                .build();

        // 发送到Kafka
        labEnvironmentProducer.sendEnvironmentDataAsync(data);
        return Result.success("🚨 已发送1条告警数据到消息队列");
    }

    /**
     * 测试批量发送
     *
     * GET http://localhost:8083/lab-monitor/test-send-batch?count=10
     */
    @GetMapping("/test-send-batch")
    public Result<String> testSendBatch(@RequestParam(defaultValue = "5") Integer count) {
        Random random = new Random();
        List<LabEnvironmentData> dataList = new ArrayList<>();

        String[] labNames = {"计算机基础实验室", "软件工程实验室", "物理光学实验室", "化学分析实验室", "生物细胞实验室"};

        for (int i = 0; i < count; i++) {
            LabEnvironmentData data = LabEnvironmentData.builder()
                    .labId((long) (random.nextInt(5) + 1))
                    .labName(labNames[random.nextInt(labNames.length)])
                    .temperature(20 + random.nextDouble() * 15)
                    .humidity(40 + random.nextDouble() * 40)
                    .pm25(10 + random.nextDouble() * 100)
                    .illuminance(300 + random.nextDouble() * 700)
                    .co2(400 + random.nextDouble() * 200)
                    .onlineDeviceCount(random.nextInt(10) + 10)
                    .totalDeviceCount(20)
                    .currentPeopleCount(random.nextInt(40) + 5)
                    .monitorTime(LocalDateTime.now())
                    .dataSource("SENSOR")
                    .build();

            dataList.add(data);
        }

        // 发送到Kafka
        labEnvironmentProducer.sendEnvironmentDataBatch(dataList);
        return Result.success("📦 已发送 " + count + " 条测试数据到消息队列");
    }

    /**
     * 健康检查
     *
     * GET http://localhost:8083/monitor/health
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> info = new HashMap<>();
        info.put("status", "UP");
        info.put("service", "monitor-service");
        info.put("timestamp", LocalDateTime.now());
        return Result.success(info);
    }

    // ========================================
    // WebSocket相关接口
    // ========================================

    /**
     * 获取WebSocket连接统计信息
     *
     * GET http://localhost:8083/lab-monitor/websocket/stats
     */
    @GetMapping("/websocket/stats")
    public Result<Map<String, Object>> getWebSocketStats() {
        Map<String, Object> stats = webSocketPushService.getConnectionStats();
        return Result.success(stats);
    }

    /**
     * 测试WebSocket推送 - 推送测试数据
     *
     * GET http://localhost:8083/lab-monitor/websocket/test-push/{labId}
     */
    @GetMapping("/websocket/test-push/{labId}")
    public Result<String> testWebSocketPush(@PathVariable Long labId) {
        try {
            // 创建测试环境数据
            LabEnvironmentData testData = LabEnvironmentData.builder()
                    .labId(labId)
                    .labName("测试实验室")
                    .temperature(22.5 + Math.random() * 5)
                    .humidity(55.0 + Math.random() * 10)
                    .pm25(30.0 + Math.random() * 20)
                    .illuminance(500.0 + Math.random() * 200)
                    .co2(600.0 + Math.random() * 200)
                    .onlineDeviceCount(15 + (int)(Math.random() * 10))
                    .totalDeviceCount(25)
                    .currentPeopleCount(20 + (int)(Math.random() * 15))
                    .monitorTime(LocalDateTime.now())
                    .dataSource("TEST")
                    .build();

            // 推送WebSocket数据
            webSocketPushService.pushEnvironmentData(testData);

            return Result.success("✅ WebSocket测试数据推送成功 - 实验室ID: " + labId);
        } catch (Exception e) {
            log.error("❌ WebSocket测试推送失败", e);
            return Result.failure("WebSocket测试推送失败: " + e.getMessage());
        }
    }

    /**
     * 测试WebSocket告警推送
     *
     * GET http://localhost:8083/lab-monitor/websocket/test-alarm/{labId}
     */
    @GetMapping("/websocket/test-alarm/{labId}")
    public Result<String> testWebSocketAlarm(@PathVariable Long labId) {
        try {
            // 创建测试告警
            LabAlarm testAlarm = new LabAlarm();
            testAlarm.setLabId(labId);
            testAlarm.setLabName("测试实验室");
            testAlarm.setAlarmType(LabAlarm.AlarmType.TEMP_HIGH.getCode());
            testAlarm.setAlarmLevel(LabAlarm.AlarmLevel.WARNING.getCode());
            testAlarm.setAlarmMessage("WebSocket测试告警：温度异常");
            testAlarm.setAlarmValue(35.5);
            testAlarm.setThresholdValue(28.0);
            testAlarm.setAlarmTime(LocalDateTime.now());
            testAlarm.setStatus(LabAlarm.HandleStatus.PENDING);

            // 推送WebSocket告警
            webSocketPushService.pushAlarm(testAlarm);

            return Result.success("🚨 WebSocket测试告警推送成功 - 实验室ID: " + labId);
        } catch (Exception e) {
            log.error("❌ WebSocket测试告警推送失败", e);
            return Result.failure("WebSocket测试告警推送失败: " + e.getMessage());
        }
    }
}
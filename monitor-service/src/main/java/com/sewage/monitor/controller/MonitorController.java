package com.sewage.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sewage.common.result.Result;
// 删除这行： import com.sewage.monitor.dto.MonitorDataDTO;
import com.sewage.monitor.entity.WaterMonitor;
import com.sewage.monitor.service.WaterMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private final WaterMonitorService waterMonitorService;

    /**
     * 上传监测数据
     */
    @PostMapping("/data")
    public Result<String> uploadData(@RequestBody WaterMonitor data) {
        log.info("接收到监测数据，处理厂ID: {}", data.getPlantId());
        waterMonitorService.saveMonitorData(data);
        return Result.success("数据上传成功");
    }

    /**
     * 获取最新监测数据
     */
    @GetMapping("/latest/{plantId}")
    public Result<WaterMonitor> getLatestData(@PathVariable Long plantId) {
        WaterMonitor data = waterMonitorService.getLatestData(plantId);
        return Result.success(data);
    }

    /**
     * 查询历史数据
     */
    @GetMapping("/history/{plantId}")
    public Result<List<WaterMonitor>> getHistoryData(
            @PathVariable Long plantId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        List<WaterMonitor> list = waterMonitorService.getHistoryData(plantId, startTime, endTime);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public Result<Page<WaterMonitor>> getPageData(
            @RequestParam(required = false) Long plantId,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        Page<WaterMonitor> page = waterMonitorService.getPageData(plantId, current, size);
        return Result.success(page);
    }

    /**
     * 测试接口
     */
    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("监控服务正在运行");
    }
}
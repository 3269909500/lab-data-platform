package com.sewage.monitor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sewage.monitor.entity.WaterMonitor;
import com.sewage.monitor.mapper.WaterMonitorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaterMonitorService {

    private final WaterMonitorMapper waterMonitorMapper;

    /**
     * 保存监测数据
     */
    public void saveMonitorData(WaterMonitor data) {
        data.setCreatedTime(LocalDateTime.now());
        // 判断水质等级
        data.setQualityLevel(calculateQualityLevel(data));
        waterMonitorMapper.insert(data);
        log.info("保存监测数据成功，处理厂ID: {}", data.getPlantId());
    }

    /**
     * 获取最新监测数据
     */
    public WaterMonitor getLatestData(Long plantId) {
        return waterMonitorMapper.getLatestData(plantId);
    }

    /**
     * 查询历史数据
     */
    public List<WaterMonitor> getHistoryData(Long plantId, LocalDateTime startTime, LocalDateTime endTime) {
        return waterMonitorMapper.getDataByTimeRange(plantId, startTime, endTime);
    }

    /**
     * 分页查询监测数据
     */
    public Page<WaterMonitor> getPageData(Long plantId, int current, int size) {
        Page<WaterMonitor> page = new Page<>(current, size);
        LambdaQueryWrapper<WaterMonitor> wrapper = new LambdaQueryWrapper<>();
        if (plantId != null) {
            wrapper.eq(WaterMonitor::getPlantId, plantId);
        }
        wrapper.orderByDesc(WaterMonitor::getMonitorTime);
        return waterMonitorMapper.selectPage(page, wrapper);
    }

    /**
     * 计算水质等级
     */
    private String calculateQualityLevel(WaterMonitor data) {
        // 简单的水质评级逻辑
        if (data.getCodValue() != null) {
            double cod = data.getCodValue().doubleValue();
            if (cod <= 15) return "I类";
            if (cod <= 20) return "II类";
            if (cod <= 30) return "III类";
            if (cod <= 40) return "IV类";
            if (cod <= 50) return "V类";
            return "劣V类";
        }
        return "未知";
    }
}
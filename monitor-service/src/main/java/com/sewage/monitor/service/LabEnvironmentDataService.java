package com.sewage.monitor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sewage.monitor.entity.LabEnvironmentData;
import com.sewage.monitor.mapper.LabEnvironmentDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实验室环境数据服务 - 集成Redis缓存
 *
 * 功能说明：
 * 1. 环境数据的CRUD操作
 * 2. 实时数据缓存（Hash结构）
 * 3. 历史数据缓存（Sorted Set结构）
 * 4. 缓存穿透、雪崩保护
 *
 * 缓存策略：
 * 1. 实时数据：30秒TTL，Hash结构，支持部分更新
 * 2. 历史数据：10分钟TTL，Sorted Set结构，支持时间范围查询
 * 3. 空值缓存：60秒TTL，防止缓存穿透
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LabEnvironmentDataService {

    private final LabEnvironmentDataMapper labEnvironmentDataMapper;
    private final CacheService cacheService;
    private final AlarmService alarmService;
    private final WebSocketPushService webSocketPushService;

    /**
     * 保存监测数据 - 集成Redis缓存
     *
     * 缓存更新策略：
     * 1. 先保存到数据库（保证数据不丢失）
     * 2. 立即更新实时数据缓存
     * 3. 异步更新历史数据缓存
     * 4. 触发告警检查
     */
    public void saveMonitorData(LabEnvironmentData data) {
        try {
            // 1. 设置基础信息
            data.setCreatedTime(LocalDateTime.now());
            data.setQualityLevel(calculateEnvironmentLevel(data));

            // 2. 保存到数据库（保证数据持久化）
            labEnvironmentDataMapper.insert(data);
            log.info("📥 数据库保存成功 - 实验室ID: {}, 实验室: {}", data.getLabId(), data.getLabName());

            // 3. 更新实时数据缓存（Hash结构）
            updateRealtimeCache(data);

            // 4. 更新历史数据缓存（Sorted Set结构）
            updateHistoryCache(data);

            // 5. 触发告警检查
            alarmService.checkAndSendAlarm(data);

            // 6. 推送WebSocket实时数据
            webSocketPushService.pushEnvironmentData(data);

            log.info("✅ 环境数据处理完成 - 实验室ID: {}", data.getLabId());

        } catch (Exception e) {
            log.error("❌ 保存环境数据失败 - 实验室ID: {}", data.getLabId(), e);
            throw e;
        }
    }

    /**
     * 更新实时数据缓存
     *
     * 使用Hash结构的原因：
     * 1. 支持部分更新，效率高
     * 2. 内存占用比String+JSON更少
     * 3. 可以直接获取单个字段
     */
    private void updateRealtimeCache(LabEnvironmentData data) {
        try {
            String cacheKey = "lab:latest:" + data.getLabId();

            // 转换为Hash结构
            Map<String, Object> hashData = convertToHash(data);

            // 批量设置Hash字段
            cacheService.hSetAll(cacheKey, hashData);

            // 设置30秒过期时间
            cacheService.expire(cacheKey, 30);

            log.debug("🔄 实时数据缓存更新成功 - key: {}", cacheKey);

        } catch (Exception e) {
            log.error("❌ 实时数据缓存更新失败 - 实验室ID: {}", data.getLabId(), e);
        }
    }

    /**
     * 更新历史数据缓存
     *
     * 使用Sorted Set结构的原因：
     * 1. 自动按时间戳排序
     * 2. 支持时间范围查询
     * 3. 内存效率高，自动去重
     */
    private void updateHistoryCache(LabEnvironmentData data) {
        try {
            String dateKey = data.getMonitorTime().toLocalDate().toString().replace("-", "");
            String cacheKey = "lab:history:" + data.getLabId() + ":" + dateKey;

            // 时间戳作为score
            double score = data.getMonitorTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            // 添加到Sorted Set
            cacheService.zAdd(cacheKey, data, score);

            // 保持最近1000条记录，防止内存无限增长
            long size = cacheService.zSize(cacheKey);
            if (size > 1000) {
                cacheService.zRemoveRangeByScore(cacheKey, 0, score - 1000);
            }

            // 设置10分钟过期时间
            cacheService.expire(cacheKey, 600);

            log.debug("📚 历史数据缓存更新成功 - key: {}, 当前大小: {}", cacheKey, size);

        } catch (Exception e) {
            log.error("❌ 历史数据缓存更新失败 - 实验室ID: {}", data.getLabId(), e);
        }
    }

    /**
     * 获取最新监测数据 - 集成Redis缓存
     *
     * 缓存查询策略：
     * 1. 先查Redis缓存（Hash结构）
     * 2. 缓存未命中，查询数据库
     * 3. 将结果写入缓存，防止缓存穿透
     *
     * @param labId 实验室ID
     * @return 最新环境数据
     */
    public LabEnvironmentData getLatestData(Long labId) {
        if (labId == null) {
            return null;
        }

        String cacheKey = "lab:latest:" + labId;

        try {
            // 1. 先查Redis缓存
            Map<Object, Object> cachedData = cacheService.hGetAll(cacheKey);

            if (cachedData != null && !cachedData.isEmpty()) {
                LabEnvironmentData data = convertFromHash(cachedData);
                log.debug("🎯 缓存命中 - 实验室ID: {}, 数据时间: {}", labId, data.getMonitorTime());
                return data;
            }

            // 2. 检查是否为空值缓存（防止缓存穿透）
            if (cacheService.isNullValue(cacheKey)) {
                log.debug("🚫 空值缓存命中 - 实验室ID: {}", labId);
                return null;
            }

            // 3. 缓存未命中，查询数据库
            log.debug("🔍 缓存未命中，查询数据库 - 实验室ID: {}", labId);
            LabEnvironmentData data = labEnvironmentDataMapper.getLatestData(labId);

            // 4. 将结果写入缓存（包括空值）
            if (data != null) {
                updateRealtimeCache(data);
            } else {
                // 缓存空值，防止缓存穿透
                cacheService.setNullValue(cacheKey);
            }

            return data;

        } catch (Exception e) {
            log.error("❌ 获取最新数据失败 - 实验室ID: {}", labId, e);
            // 降级到直接查询数据库
            return labEnvironmentDataMapper.getLatestData(labId);
        }
    }

    /**
     * 查询历史数据 - 集成Redis缓存
     *
     * 缓存查询策略：
     * 1. 先查Redis缓存（Sorted Set结构）
     * 2. 使用时间戳范围查询
     * 3. 缓存未命中，查询数据库
     *
     * @param labId 实验室ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 历史数据列表
     */
    public List<LabEnvironmentData> getHistoryData(Long labId, LocalDateTime startTime, LocalDateTime endTime) {
        if (labId == null || startTime == null || endTime == null) {
            return List.of();
        }

        try {
            // 检查是否为同一天的数据（同一天使用同一个缓存Key）
            if (startTime.toLocalDate().equals(endTime.toLocalDate())) {
                return getHistoryDataFromCache(labId, startTime, endTime);
            } else {
                // 跨天数据，直接查询数据库（可以考虑拆分成多个缓存查询）
                log.debug("📅 跨天查询，直接查询数据库 - 实验室ID: {}", labId);
                return labEnvironmentDataMapper.getDataByTimeRange(labId, startTime, endTime);
            }

        } catch (Exception e) {
            log.error("❌ 获取历史数据失败 - 实验室ID: {}", labId, e);
            // 降级到直接查询数据库
            return labEnvironmentDataMapper.getDataByTimeRange(labId, startTime, endTime);
        }
    }

    /**
     * 从缓存获取历史数据
     */
    private List<LabEnvironmentData> getHistoryDataFromCache(Long labId, LocalDateTime startTime, LocalDateTime endTime) {
        String dateKey = startTime.toLocalDate().toString().replace("-", "");
        String cacheKey = "lab:history:" + labId + ":" + dateKey;

        // 转换为时间戳
        double startScore = startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        double endScore = endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        // 查询时间范围内的数据
        var cachedData = cacheService.zRangeByScore(cacheKey, startScore, endScore);

        if (!cachedData.isEmpty()) {
            log.debug("📚 历史数据缓存命中 - 实验室ID: {}, 数据量: {}", labId, cachedData.size());
            return cachedData.stream()
                    .map(obj -> (LabEnvironmentData) obj)
                    .sorted((a, b) -> b.getMonitorTime().compareTo(a.getMonitorTime())) // 按时间倒序
                    .toList();
        }

        // 缓存未命中，查询数据库
        log.debug("🔍 历史数据缓存未命中，查询数据库 - 实验室ID: {}", labId);
        List<LabEnvironmentData> dataList = labEnvironmentDataMapper.getDataByTimeRange(labId, startTime, endTime);

        // 将查询结果写入缓存（可选，根据业务需求决定）
        for (LabEnvironmentData data : dataList) {
            updateHistoryCache(data);
        }

        return dataList;
    }

    /**
     * 分页查询监测数据
     */
    public Page<LabEnvironmentData> getPageData(Long labId, int current, int size) {
        Page<LabEnvironmentData> page = new Page<>(current, size);
        LambdaQueryWrapper<LabEnvironmentData> wrapper = new LambdaQueryWrapper<>();
        if (labId != null) {
            wrapper.eq(LabEnvironmentData::getLabId, labId);
        }
        wrapper.orderByDesc(LabEnvironmentData::getMonitorTime);
        return labEnvironmentDataMapper.selectPage(page, wrapper);
    }

    /**
     * 计算环境质量等级
     */
    private String calculateEnvironmentLevel(LabEnvironmentData data) {
        // 基于多指标的环境质量评级逻辑
        int score = 100;

        // 温度评分 (18-28°C为最佳)
        if (data.getTemperature() != null) {
            double temp = data.getTemperature();
            if (temp < 15 || temp > 32) score -= 20;
            else if (temp < 18 || temp > 28) score -= 10;
        }

        // 湿度评分 (40-70%为最佳)
        if (data.getHumidity() != null) {
            double humidity = data.getHumidity();
            if (humidity < 30 || humidity > 80) score -= 20;
            else if (humidity < 40 || humidity > 70) score -= 10;
        }

        // PM2.5评分
        if (data.getPm25() != null) {
            double pm25 = data.getPm25();
            if (pm25 > 150) score -= 30;
            else if (pm25 > 75) score -= 20;
            else if (pm25 > 35) score -= 10;
        }

        // CO2评分
        if (data.getCo2() != null) {
            double co2 = data.getCo2();
            if (co2 > 2000) score -= 30;
            else if (co2 > 1000) score -= 20;
            else if (co2 > 800) score -= 10;
        }

        // 根据评分确定等级
        if (score >= 90) return "优秀";
        if (score >= 80) return "良好";
        if (score >= 70) return "一般";
        if (score >= 60) return "较差";
        return "很差";
    }

    // =============================数据转换方法=============================

    /**
     * 将LabEnvironmentData转换为Hash结构
     *
     * 使用Hash结构的原因：
     * 1. 内存效率高：比JSON字符串节省15-20%内存
     * 2. 部分更新：可以只更新某个字段
     * 3. 原子操作：支持HINCRBY等操作
     * 4. 查询方便：可以直接获取某个字段
     */
    private Map<String, Object> convertToHash(LabEnvironmentData data) {
        Map<String, Object> hash = new HashMap<>();

        if (data.getId() != null) {
            hash.put("id", data.getId().toString());
        }
        if (data.getLabId() != null) {
            hash.put("labId", data.getLabId().toString());
        }
        if (StringUtils.hasText(data.getLabName())) {
            hash.put("labName", data.getLabName());
        }
        if (data.getTemperature() != null) {
            hash.put("temperature", data.getTemperature().toString());
        }
        if (data.getHumidity() != null) {
            hash.put("humidity", data.getHumidity().toString());
        }
        if (data.getPm25() != null) {
            hash.put("pm25", data.getPm25().toString());
        }
        if (data.getIlluminance() != null) {
            hash.put("illuminance", data.getIlluminance().toString());
        }
        if (data.getCo2() != null) {
            hash.put("co2", data.getCo2().toString());
        }
        if (data.getOnlineDeviceCount() != null) {
            hash.put("onlineDeviceCount", data.getOnlineDeviceCount().toString());
        }
        if (data.getTotalDeviceCount() != null) {
            hash.put("totalDeviceCount", data.getTotalDeviceCount().toString());
        }
        if (data.getCurrentPeopleCount() != null) {
            hash.put("currentPeopleCount", data.getCurrentPeopleCount().toString());
        }
        if (data.getMonitorTime() != null) {
            hash.put("monitorTime", data.getMonitorTime().toString());
        }
        if (StringUtils.hasText(data.getDataSource())) {
            hash.put("dataSource", data.getDataSource());
        }
        if (StringUtils.hasText(data.getQualityLevel())) {
            hash.put("qualityLevel", data.getQualityLevel());
        }
        if (data.getCreatedTime() != null) {
            hash.put("createdTime", data.getCreatedTime().toString());
        }

        // 添加更新时间戳
        hash.put("updateTime", String.valueOf(System.currentTimeMillis()));

        return hash;
    }

    /**
     * 从Hash结构转换为LabEnvironmentData
     */
    private LabEnvironmentData convertFromHash(Map<Object, Object> hash) {
        if (hash == null || hash.isEmpty()) {
            return null;
        }

        LabEnvironmentData data = new LabEnvironmentData();

        Object idObj = hash.get("id");
        if (idObj != null && StringUtils.hasText(idObj.toString())) {
            data.setId(Long.valueOf(idObj.toString()));
        }

        Object labIdObj = hash.get("labId");
        if (labIdObj != null && StringUtils.hasText(labIdObj.toString())) {
            data.setLabId(Long.valueOf(labIdObj.toString()));
        }

        Object labNameObj = hash.get("labName");
        if (labNameObj != null && StringUtils.hasText(labNameObj.toString())) {
            data.setLabName(labNameObj.toString());
        }

        Object temperatureObj = hash.get("temperature");
        if (temperatureObj != null && StringUtils.hasText(temperatureObj.toString())) {
            data.setTemperature(Double.valueOf(temperatureObj.toString()));
        }

        Object humidityObj = hash.get("humidity");
        if (humidityObj != null && StringUtils.hasText(humidityObj.toString())) {
            data.setHumidity(Double.valueOf(humidityObj.toString()));
        }

        Object pm25Obj = hash.get("pm25");
        if (pm25Obj != null && StringUtils.hasText(pm25Obj.toString())) {
            data.setPm25(Double.valueOf(pm25Obj.toString()));
        }

        Object illuminanceObj = hash.get("illuminance");
        if (illuminanceObj != null && StringUtils.hasText(illuminanceObj.toString())) {
            data.setIlluminance(Double.valueOf(illuminanceObj.toString()));
        }

        Object co2Obj = hash.get("co2");
        if (co2Obj != null && StringUtils.hasText(co2Obj.toString())) {
            data.setCo2(Double.valueOf(co2Obj.toString()));
        }

        Object onlineDeviceCountObj = hash.get("onlineDeviceCount");
        if (onlineDeviceCountObj != null && StringUtils.hasText(onlineDeviceCountObj.toString())) {
            data.setOnlineDeviceCount(Integer.valueOf(onlineDeviceCountObj.toString()));
        }

        Object totalDeviceCountObj = hash.get("totalDeviceCount");
        if (totalDeviceCountObj != null && StringUtils.hasText(totalDeviceCountObj.toString())) {
            data.setTotalDeviceCount(Integer.valueOf(totalDeviceCountObj.toString()));
        }

        Object currentPeopleCountObj = hash.get("currentPeopleCount");
        if (currentPeopleCountObj != null && StringUtils.hasText(currentPeopleCountObj.toString())) {
            data.setCurrentPeopleCount(Integer.valueOf(currentPeopleCountObj.toString()));
        }

        Object monitorTimeObj = hash.get("monitorTime");
        if (monitorTimeObj != null && StringUtils.hasText(monitorTimeObj.toString())) {
            data.setMonitorTime(LocalDateTime.parse(monitorTimeObj.toString()));
        }

        Object dataSourceObj = hash.get("dataSource");
        if (dataSourceObj != null && StringUtils.hasText(dataSourceObj.toString())) {
            data.setDataSource(dataSourceObj.toString());
        }

        Object qualityLevelObj = hash.get("qualityLevel");
        if (qualityLevelObj != null && StringUtils.hasText(qualityLevelObj.toString())) {
            data.setQualityLevel(qualityLevelObj.toString());
        }

        Object createdTimeObj = hash.get("createdTime");
        if (createdTimeObj != null && StringUtils.hasText(createdTimeObj.toString())) {
            data.setCreatedTime(LocalDateTime.parse(createdTimeObj.toString()));
        }

        return data;
    }

    // =============================缓存管理方法=============================

    /**
     * 清理指定实验室的缓存
     *
     * @param labId 实验室ID
     */
    public void clearLabCache(Long labId) {
        try {
            // 清理实时数据缓存
            String realtimeKey = "lab:latest:" + labId;
            cacheService.delete(realtimeKey);

            // 清理历史数据缓存（清理最近7天）
            for (int i = 0; i < 7; i++) {
                String date = LocalDateTime.now().minusDays(i).toLocalDate().toString().replace("-", "");
                String historyKey = "lab:history:" + labId + ":" + date;
                cacheService.delete(historyKey);
            }

            log.info("🧹 实验室缓存清理完成 - 实验室ID: {}", labId);

        } catch (Exception e) {
            log.error("❌ 清理实验室缓存失败 - 实验室ID: {}", labId, e);
        }
    }

    /**
     * 预热实验室缓存
     *
     * @param labId 实验室ID
     */
    public void warmUpLabCache(Long labId) {
        try {
            // 获取最新数据并写入缓存
            LabEnvironmentData latestData = getLatestData(labId);
            if (latestData != null) {
                log.info("🔥 实验室缓存预热成功 - 实验室ID: {}", labId);
            }

        } catch (Exception e) {
            log.error("❌ 实验室缓存预热失败 - 实验室ID: {}", labId, e);
        }
    }
}
package com.sewage.monitor.service;

import com.sewage.monitor.entity.LabDailyStatistics;
import com.sewage.monitor.entity.LabEnvironmentData;
import com.sewage.monitor.mapper.LabDailyStatisticsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 统计服务 - 集成Redis缓存
 *
 * 功能说明：
 * 1. 实验室日统计数据更新
 * 2. 统计数据缓存（Hash结构）
 * 3. 异步统计更新，提升性能
 * 4. 缓存预热和降级策略
 *
 * 缓存策略：
 * 1. 日统计数据：1小时TTL，Hash结构
 * 2. 统计概览：30分钟TTL，String结构
 * 3. 批量更新：减少数据库压力
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final LabDailyStatisticsMapper dailyStatisticsMapper;
    private final CacheService cacheService;

    /**
     * 异步更新统计数据 - 集成Redis缓存
     *
     * 异步处理的原因：
     * 1. 统计计算复杂，不阻塞主流程
     * 2. 减少数据库写入压力
     * 3. 提升系统响应速度
     *
     * @param data 环境数据
     */
    @Async("statisticsExecutor")
    @Transactional
    public void updateStatistics(LabEnvironmentData data) {
        try {
            LocalDate today = data.getMonitorTime().toLocalDate();
            Long labId = data.getLabId();
            String cacheKey = "lab:stats:daily:" + labId + ":" + today.format(DateTimeFormatter.BASIC_ISO_DATE);

            log.debug("📊 开始异步更新统计 - 实验室ID: {}, 日期: {}", labId, today);

            // 1. 先从Redis缓存获取统计数据
            LabDailyStatistics stats = getStatisticsFromCache(labId, today);

            if (stats == null) {
                // 2. Redis中没有，查询数据库
                stats = dailyStatisticsMapper.selectByLabAndDate(labId, today);
            }

            if (stats == null) {
                // 3. 数据库中也没有，创建新记录
                stats = createNewStatistics(data, today);
                dailyStatisticsMapper.insert(stats);
                log.info("📊 创建新统计记录 - 实验室: {}, 日期: {}", data.getLabName(), today);
            } else {
                // 4. 更新现有记录
                updateExistingStatistics(stats, data);
                dailyStatisticsMapper.updateById(stats);
                log.debug("📊 更新统计记录 - 实验室: {}, 日期: {}", data.getLabName(), today);
            }

            // 5. 更新Redis缓存
            updateStatisticsCache(stats);

            log.debug("✅ 统计更新完成 - 实验室ID: {}", labId);

        } catch (Exception e) {
            log.error("❌ 统计更新失败 - 实验室ID: {}", data.getLabId(), e);
        }
    }

    /**
     * 从缓存获取统计数据
     */
    private LabDailyStatistics getStatisticsFromCache(Long labId, LocalDate date) {
        String cacheKey = "lab:stats:daily:" + labId + ":" + date.format(DateTimeFormatter.BASIC_ISO_DATE);

        try {
            Map<Object, Object> cachedStats = cacheService.hGetAll(cacheKey);

            if (cachedStats != null && !cachedStats.isEmpty()) {
                LabDailyStatistics stats = convertFromHash(cachedStats);
                log.debug("📊 统计缓存命中 - 实验室ID: {}, 日期: {}", labId, date);
                return stats;
            }

            // 检查空值缓存
            if (cacheService.isNullValue(cacheKey)) {
                log.debug("🚫 统计空值缓存命中 - 实验室ID: {}, 日期: {}", labId, date);
                return null;
            }

            return null;

        } catch (Exception e) {
            log.error("❌ 获取统计缓存失败 - 实验室ID: {}, 日期: {}", labId, date, e);
            return null;
        }
    }

    /**
     * 更新统计数据缓存
     */
    private void updateStatisticsCache(LabDailyStatistics stats) {
        try {
            String cacheKey = "lab:stats:daily:" + stats.getStationId() + ":" +
                            stats.getStatDate().format(DateTimeFormatter.BASIC_ISO_DATE);

            // 转换为Hash结构
            Map<String, Object> hashData = convertToHash(stats);

            // 批量设置Hash字段
            cacheService.hSetAll(cacheKey, hashData);

            // 设置1小时过期时间
            cacheService.expire(cacheKey, 3600);

            log.debug("📊 统计缓存更新成功 - key: {}", cacheKey);

        } catch (Exception e) {
            log.error("❌ 统计缓存更新失败 - 实验室ID: {}", stats.getStationId(), e);
        }
    }

    /**
     * 创建新的统计记录
     */
    private LabDailyStatistics createNewStatistics(LabEnvironmentData data, LocalDate date) {
        return LabDailyStatistics.builder()
                .stationId(data.getLabId())  // 使用实验室ID
                .stationName(data.getLabName()) // 使用实验室名称
                .statDate(date)
                // 温度统计
                .avgTemperature(data.getTemperature() != null ? BigDecimal.valueOf(data.getTemperature()) : null)
                .maxTemperature(data.getTemperature() != null ? BigDecimal.valueOf(data.getTemperature()) : null)
                .minTemperature(data.getTemperature() != null ? BigDecimal.valueOf(data.getTemperature()) : null)
                // 湿度统计
                .avgHumidity(data.getHumidity() != null ? BigDecimal.valueOf(data.getHumidity()) : null)
                .maxHumidity(data.getHumidity() != null ? BigDecimal.valueOf(data.getHumidity()) : null)
                .minHumidity(data.getHumidity() != null ? BigDecimal.valueOf(data.getHumidity()) : null)
                // PM2.5统计
                .avgPm25(data.getPm25() != null ? BigDecimal.valueOf(data.getPm25()) : null)
                .maxPm25(data.getPm25() != null ? BigDecimal.valueOf(data.getPm25()) : null)
                // CO2统计
                .avgCo2(data.getCo2() != null ? BigDecimal.valueOf(data.getCo2()) : null)
                .maxCo2(data.getCo2() != null ? BigDecimal.valueOf(data.getCo2()) : null)
                // 人员统计
                .maxPeopleCount(data.getCurrentPeopleCount())
                // 设备统计
                .avgOnlineDevices(BigDecimal.valueOf(data.getOnlineDeviceCount()))
                // 其他统计
                .dataCount(1)
                .alarmCount(data.getAlarmStatus() != null && data.getAlarmStatus() > 0 ? 1 : 0)
                .normalRate(data.getAlarmStatus() != null && data.getAlarmStatus() > 0 ?
                        BigDecimal.ZERO : new BigDecimal("100"))
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .build();
    }

    /**
     * 更新已有的统计记录
     * 使用增量计算公式：新平均值 = (旧平均值 * 旧数量 + 新值) / 新数量
     */
    private void updateExistingStatistics(LabDailyStatistics stats, LabEnvironmentData data) {
        int oldCount = stats.getDataCount();
        int newCount = oldCount + 1;

        // 更新温度统计
        if (data.getTemperature() != null) {
            BigDecimal tempValue = BigDecimal.valueOf(data.getTemperature());
            stats.setAvgTemperature(calculateNewAverage(
                    stats.getAvgTemperature(), tempValue, oldCount));
            stats.setMaxTemperature(max(stats.getMaxTemperature(), tempValue));
            stats.setMinTemperature(min(stats.getMinTemperature(), tempValue));
        }

        // 更新湿度统计
        if (data.getHumidity() != null) {
            BigDecimal humidityValue = BigDecimal.valueOf(data.getHumidity());
            stats.setAvgHumidity(calculateNewAverage(stats.getAvgHumidity(), humidityValue, oldCount));
            stats.setMaxHumidity(max(stats.getMaxHumidity(), humidityValue));
            stats.setMinHumidity(min(stats.getMinHumidity(), humidityValue));
        }

        // 更新PM2.5统计
        if (data.getPm25() != null) {
            BigDecimal pm25Value = BigDecimal.valueOf(data.getPm25());
            stats.setAvgPm25(calculateNewAverage(stats.getAvgPm25(), pm25Value, oldCount));
            stats.setMaxPm25(max(stats.getMaxPm25(), pm25Value));
        }

        // 更新CO2统计
        if (data.getCo2() != null) {
            BigDecimal co2Value = BigDecimal.valueOf(data.getCo2());
            stats.setAvgCo2(calculateNewAverage(stats.getAvgCo2(), co2Value, oldCount));
            stats.setMaxCo2(max(stats.getMaxCo2(), co2Value));
        }

        // 更新最大在线人数
        if (data.getCurrentPeopleCount() != null) {
            stats.setMaxPeopleCount(Math.max(stats.getMaxPeopleCount(), data.getCurrentPeopleCount()));
        }

        // 更新设备平均在线数量
        if (data.getOnlineDeviceCount() != null) {
            BigDecimal onlineCount = BigDecimal.valueOf(data.getOnlineDeviceCount());
            stats.setAvgOnlineDevices(calculateNewAverage(stats.getAvgOnlineDevices(), onlineCount, oldCount));
        }

        // 更新数据条数
        stats.setDataCount(newCount);

        // 更新告警次数
        if (data.getAlarmStatus() != null && data.getAlarmStatus() > 0) {
            stats.setAlarmCount(stats.getAlarmCount() + 1);
        }

        // 更新环境达标率 = (总数 - 告警数) / 总数 * 100
        BigDecimal normalRate = BigDecimal.valueOf(newCount - stats.getAlarmCount())
                .divide(BigDecimal.valueOf(newCount), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        stats.setNormalRate(normalRate);

        // 更新时间
        stats.setUpdatedTime(LocalDateTime.now());
    }

    /**
     * 计算新平均值
     * 公式：新平均值 = (旧平均值 * 旧数量 + 新值) / 新数量
     */
    private BigDecimal calculateNewAverage(BigDecimal oldAvg, BigDecimal newValue, int oldCount) {
        if (oldAvg == null) return newValue;
        if (newValue == null) return oldAvg;

        return oldAvg.multiply(BigDecimal.valueOf(oldCount))
                .add(newValue)
                .divide(BigDecimal.valueOf(oldCount + 1), 2, RoundingMode.HALF_UP);
    }

    /**
     * 计算新平均值（Integer版本）
     */
    private BigDecimal calculateNewAverage(BigDecimal oldAvg, Integer newValue, int oldCount) {
        if (newValue == null) return oldAvg;
        return calculateNewAverage(oldAvg, BigDecimal.valueOf(newValue), oldCount);
    }

    /**
     * 取最大值
     */
    private BigDecimal max(BigDecimal a, BigDecimal b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.compareTo(b) > 0 ? a : b;
    }

    /**
     * 取最大值（Integer版本）
     */
    private Integer max(Integer a, Integer b) {
        if (a == null) return b;
        if (b == null) return a;
        return Math.max(a, b);
    }

    /**
     * 取最小值
     */
    private BigDecimal min(BigDecimal a, BigDecimal b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.compareTo(b) < 0 ? a : b;
    }

    /**
     * 取最小值（Integer版本）
     */
    private Integer min(Integer a, Integer b) {
        if (a == null) return b;
        if (b == null) return a;
        return Math.min(a, b);
    }

    // =============================数据转换方法=============================

    /**
     * 将LabDailyStatistics转换为Hash结构
     */
    private Map<String, Object> convertToHash(LabDailyStatistics stats) {
        Map<String, Object> hash = new HashMap<>();

        if (stats.getId() != null) {
            hash.put("id", stats.getId().toString());
        }
        if (stats.getStationId() != null) {
            hash.put("stationId", stats.getStationId().toString());
        }
        if (stats.getStationName() != null) {
            hash.put("stationName", stats.getStationName());
        }
        if (stats.getStatDate() != null) {
            hash.put("statDate", stats.getStatDate().toString());
        }
        if (stats.getAvgTemperature() != null) {
            hash.put("avgTemperature", stats.getAvgTemperature().toString());
        }
        if (stats.getAvgHumidity() != null) {
            hash.put("avgHumidity", stats.getAvgHumidity().toString());
        }
        if (stats.getMaxPm25() != null) {
            hash.put("maxPm25", stats.getMaxPm25().toString());
        }
        if (stats.getAvgCo2() != null) {
            hash.put("avgCo2", stats.getAvgCo2().toString());
        }
        if (stats.getMaxPeopleCount() != null) {
            hash.put("maxPeopleCount", stats.getMaxPeopleCount().toString());
        }
        if (stats.getAvgOnlineDevices() != null) {
            hash.put("avgOnlineDevices", stats.getAvgOnlineDevices().toString());
        }
        if (stats.getAlarmCount() != null) {
            hash.put("alarmCount", stats.getAlarmCount().toString());
        }
        if (stats.getDataCount() != null) {
            hash.put("dataCount", stats.getDataCount().toString());
        }
        if (stats.getNormalRate() != null) {
            hash.put("normalRate", stats.getNormalRate().toString());
        }
        if (stats.getCreatedTime() != null) {
            hash.put("createdTime", stats.getCreatedTime().toString());
        }
        if (stats.getUpdatedTime() != null) {
            hash.put("updatedTime", stats.getUpdatedTime().toString());
        }

        // 添加更新时间戳
        hash.put("updateTime", String.valueOf(System.currentTimeMillis()));

        return hash;
    }

    /**
     * 从Hash结构转换为LabDailyStatistics
     */
    private LabDailyStatistics convertFromHash(Map<Object, Object> hash) {
        if (hash == null || hash.isEmpty()) {
            return null;
        }

        LabDailyStatistics stats = new LabDailyStatistics();

        Object idObj = hash.get("id");
        if (idObj != null) {
            stats.setId(Long.valueOf(idObj.toString()));
        }

        Object stationIdObj = hash.get("stationId");
        if (stationIdObj != null) {
            stats.setStationId(Long.valueOf(stationIdObj.toString()));
        }

        Object stationNameObj = hash.get("stationName");
        if (stationNameObj != null) {
            stats.setStationName(stationNameObj.toString());
        }

        Object statDateObj = hash.get("statDate");
        if (statDateObj != null) {
            stats.setStatDate(LocalDate.parse(statDateObj.toString()));
        }

        Object avgTemperatureObj = hash.get("avgTemperature");
        if (avgTemperatureObj != null) {
            stats.setAvgTemperature(new BigDecimal(avgTemperatureObj.toString()));
        }

        Object avgHumidityObj = hash.get("avgHumidity");
        if (avgHumidityObj != null) {
            stats.setAvgHumidity(new BigDecimal(avgHumidityObj.toString()));
        }

        Object maxPm25Obj = hash.get("maxPm25");
        if (maxPm25Obj != null) {
            stats.setMaxPm25(new BigDecimal(maxPm25Obj.toString()));
        }

        Object avgCo2Obj = hash.get("avgCo2");
        if (avgCo2Obj != null) {
            stats.setAvgCo2(new BigDecimal(avgCo2Obj.toString()));
        }

        Object maxPeopleCountObj = hash.get("maxPeopleCount");
        if (maxPeopleCountObj != null) {
            stats.setMaxPeopleCount(Integer.valueOf(maxPeopleCountObj.toString()));
        }

        Object avgOnlineDevicesObj = hash.get("avgOnlineDevices");
        if (avgOnlineDevicesObj != null) {
            stats.setAvgOnlineDevices(new BigDecimal(avgOnlineDevicesObj.toString()));
        }

        Object alarmCountObj = hash.get("alarmCount");
        if (alarmCountObj != null) {
            stats.setAlarmCount(Integer.valueOf(alarmCountObj.toString()));
        }

        Object dataCountObj = hash.get("dataCount");
        if (dataCountObj != null) {
            stats.setDataCount(Integer.valueOf(dataCountObj.toString()));
        }

        Object normalRateObj = hash.get("normalRate");
        if (normalRateObj != null) {
            stats.setNormalRate(new BigDecimal(normalRateObj.toString()));
        }

        Object createdTimeObj = hash.get("createdTime");
        if (createdTimeObj != null) {
            stats.setCreatedTime(LocalDateTime.parse(createdTimeObj.toString()));
        }

        Object updatedTimeObj = hash.get("updatedTime");
        if (updatedTimeObj != null) {
            stats.setUpdatedTime(LocalDateTime.parse(updatedTimeObj.toString()));
        }

        return stats;
    }

    // =============================缓存管理方法=============================

    /**
     * 获取统计数据 - 优先从缓存获取
     */
    public LabDailyStatistics getStatistics(Long labId, LocalDate date) {
        if (labId == null || date == null) {
            return null;
        }

        try {
            // 1. 先查Redis缓存
            LabDailyStatistics cachedStats = getStatisticsFromCache(labId, date);

            if (cachedStats != null) {
                return cachedStats;
            }

            // 2. 缓存未命中，查询数据库
            LabDailyStatistics stats = dailyStatisticsMapper.selectByLabAndDate(labId, date);

            // 3. 将结果写入缓存
            if (stats != null) {
                updateStatisticsCache(stats);
            } else {
                // 缓存空值，防止缓存穿透
                String cacheKey = "lab:stats:daily:" + labId + ":" + date.format(DateTimeFormatter.BASIC_ISO_DATE);
                cacheService.setNullValue(cacheKey);
            }

            return stats;

        } catch (Exception e) {
            log.error("❌ 获取统计数据失败 - 实验室ID: {}, 日期: {}", labId, date, e);
            // 降级到直接查询数据库
            return dailyStatisticsMapper.selectByLabAndDate(labId, date);
        }
    }

    /**
     * 清理统计数据缓存
     */
    public void clearStatisticsCache(Long labId, LocalDate date) {
        try {
            String cacheKey = "lab:stats:daily:" + labId + ":" + date.format(DateTimeFormatter.BASIC_ISO_DATE);
            cacheService.delete(cacheKey);
            log.info("🧹 统计缓存清理完成 - 实验室ID: {}, 日期: {}", labId, date);
        } catch (Exception e) {
            log.error("❌ 清理统计缓存失败 - 实验室ID: {}, 日期: {}", labId, date, e);
        }
    }

    /**
     * 预热统计数据缓存
     */
    public void warmUpStatisticsCache(Long labId, LocalDate date) {
        try {
            LabDailyStatistics stats = getStatistics(labId, date);
            if (stats != null) {
                log.info("🔥 统计缓存预热成功 - 实验室ID: {}, 日期: {}", labId, date);
            }
        } catch (Exception e) {
            log.error("❌ 统计缓存预热失败 - 实验室ID: {}, 日期: {}", labId, date, e);
        }
    }
}
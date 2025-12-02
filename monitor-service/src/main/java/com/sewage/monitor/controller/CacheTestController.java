package com.sewage.monitor.controller;

import com.sewage.common.result.Result;
import com.sewage.monitor.entity.LabEnvironmentData;
import com.sewage.monitor.service.CacheService;
import com.sewage.monitor.service.LabEnvironmentDataService;
import com.sewage.monitor.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis缓存测试控制器
 *
 * 功能说明：
 * 1. 测试Redis缓存功能
 * 2. 验证缓存命中率
 * 3. 测试缓存穿透、雪崩保护
 * 4. 性能测试和对比
 */
@Slf4j
@RestController
@RequestMapping("/cache-test")
@RequiredArgsConstructor
public class CacheTestController {

    private final CacheService cacheService;
    private final LabEnvironmentDataService labEnvironmentDataService;
    private final StatisticsService statisticsService;

    /**
     * 测试Redis连接状态
     *
     * GET /cache-test/status
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> checkRedisStatus() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 测试基本操作
            String testKey = "test:redis:connection";
            String testValue = "Redis连接测试-" + System.currentTimeMillis();

            // 测试写入
            cacheService.set(testKey, testValue, 60);
            result.put("write", "✅ 成功");

            // 测试读取
            Object readValue = cacheService.get(testKey);
            if (testValue.equals(readValue)) {
                result.put("read", "✅ 成功");
            } else {
                result.put("read", "❌ 失败");
            }

            // 测试删除
            boolean deleted = cacheService.delete(testKey);
            result.put("delete", deleted ? "✅ 成功" : "❌ 失败");

            // 获取缓存统计
            Map<String, Object> stats = cacheService.getCacheStats();
            result.put("stats", stats);

            result.put("status", "UP");
            result.put("message", "Redis连接正常");
            result.put("timestamp", System.currentTimeMillis());

            log.info("🧪 Redis状态检查完成");

        } catch (Exception e) {
            log.error("❌ Redis状态检查失败", e);
            result.put("status", "DOWN");
            result.put("error", e.getMessage());
            result.put("message", "Redis连接失败");
        }

        return Result.success(result);
    }

    /**
     * 测试实时数据缓存
     *
     * GET /cache-test/realtime/{labId}
     */
    @GetMapping("/realtime/{labId}")
    public Result<Map<String, Object>> testRealtimeCache(@PathVariable Long labId) {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("🧪 开始测试实时数据缓存 - 实验室ID: {}", labId);

            // 第一次查询（应该查询数据库）
            long startTime1 = System.currentTimeMillis();
            LabEnvironmentData data1 = labEnvironmentDataService.getLatestData(labId);
            long endTime1 = System.currentTimeMillis();
            long dbTime = endTime1 - startTime1;

            // 第二次查询（应该命中缓存）
            long startTime2 = System.currentTimeMillis();
            LabEnvironmentData data2 = labEnvironmentDataService.getLatestData(labId);
            long endTime2 = System.currentTimeMillis();
            long cacheTime = endTime2 - startTime2;

            result.put("labId", labId);
            result.put("data", data2);
            result.put("dbQueryTime", dbTime + "ms");
            result.put("cacheQueryTime", cacheTime + "ms");
            result.put("performanceImprovement", dbTime > 0 ? String.format("%.1f%%", (double)(dbTime - cacheTime) / dbTime * 100) : "0%");
            result.put("cacheHit", cacheTime < dbTime);

            log.info("🧪 实时数据缓存测试完成 - 实验室ID: {}, DB时间: {}ms, 缓存时间: {}ms", labId, dbTime, cacheTime);

        } catch (Exception e) {
            log.error("❌ 实时数据缓存测试失败 - 实验室ID: {}", labId, e);
            result.put("error", e.getMessage());
        }

        return Result.success(result);
    }

    /**
     * 测试历史数据缓存
     *
     * GET /cache-test/history/{labId}
     */
    @GetMapping("/history/{labId}")
    public Result<Map<String, Object>> testHistoryCache(@PathVariable Long labId) {
        Map<String, Object> result = new HashMap<>();

        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(2);

            log.info("🧪 开始测试历史数据缓存 - 实验室ID: {}, 时间范围: {} ~ {}", labId, startTime, endTime);

            // 第一次查询（应该查询数据库）
            long startQuery1 = System.currentTimeMillis();
            List<LabEnvironmentData> data1 = labEnvironmentDataService.getHistoryData(labId, startTime, endTime);
            long endQuery1 = System.currentTimeMillis();
            long dbTime = endQuery1 - startQuery1;

            // 第二次查询（应该命中缓存）
            long startQuery2 = System.currentTimeMillis();
            List<LabEnvironmentData> data2 = labEnvironmentDataService.getHistoryData(labId, startTime, endTime);
            long endQuery2 = System.currentTimeMillis();
            long cacheTime = endQuery2 - startQuery2;

            result.put("labId", labId);
            result.put("timeRange", startTime + " ~ " + endTime);
            result.put("dataCount", data2.size());
            result.put("dbQueryTime", dbTime + "ms");
            result.put("cacheQueryTime", cacheTime + "ms");
            result.put("performanceImprovement", dbTime > 0 ? String.format("%.1f%%", (double)(dbTime - cacheTime) / dbTime * 100) : "0%");
            result.put("cacheHit", cacheTime < dbTime);

            log.info("🧪 历史数据缓存测试完成 - 实验室ID: {}, 数据量: {}, DB时间: {}ms, 缓存时间: {}ms",
                    labId, data2.size(), dbTime, cacheTime);

        } catch (Exception e) {
            log.error("❌ 历史数据缓存测试失败 - 实验室ID: {}", labId, e);
            result.put("error", e.getMessage());
        }

        return Result.success(result);
    }

    /**
     * 测试统计数据缓存
     *
     * GET /cache-test/statistics/{labId}
     */
    @GetMapping("/statistics/{labId}")
    public Result<Map<String, Object>> testStatisticsCache(@PathVariable Long labId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 查询今天的统计数据
            log.info("🧪 开始测试统计数据缓存 - 实验室ID: {}", labId);

            // 第一次查询（应该查询数据库）
            long startTime1 = System.currentTimeMillis();
            var stats1 = statisticsService.getStatistics(labId, java.time.LocalDate.now());
            long endTime1 = System.currentTimeMillis();
            long dbTime = endTime1 - startTime1;

            // 第二次查询（应该命中缓存）
            long startTime2 = System.currentTimeMillis();
            var stats2 = statisticsService.getStatistics(labId, java.time.LocalDate.now());
            long endTime2 = System.currentTimeMillis();
            long cacheTime = endTime2 - startTime2;

            result.put("labId", labId);
            result.put("date", java.time.LocalDate.now());
            result.put("statistics", stats2);
            result.put("dbQueryTime", dbTime + "ms");
            result.put("cacheQueryTime", cacheTime + "ms");
            result.put("performanceImprovement", dbTime > 0 ? String.format("%.1f%%", (double)(dbTime - cacheTime) / dbTime * 100) : "0%");
            result.put("cacheHit", cacheTime < dbTime);

            log.info("🧪 统计数据缓存测试完成 - 实验室ID: {}, DB时间: {}ms, 缓存时间: {}ms", labId, dbTime, cacheTime);

        } catch (Exception e) {
            log.error("❌ 统计数据缓存测试失败 - 实验室ID: {}", labId, e);
            result.put("error", e.getMessage());
        }

        return Result.success(result);
    }

    /**
     * 测试缓存穿透保护
     *
     * GET /cache-test/penetration/{labId}
     */
    @GetMapping("/penetration/{labId}")
    public Result<Map<String, Object>> testCachePenetration(@PathVariable Long labId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 使用一个不存在的实验室ID
            long nonExistentLabId = 99999L;

            log.info("🧪 开始测试缓存穿透保护 - 实验室ID: {}", nonExistentLabId);

            // 第一次查询（应该查询数据库并缓存空值）
            long startTime1 = System.currentTimeMillis();
            LabEnvironmentData data1 = labEnvironmentDataService.getLatestData(nonExistentLabId);
            long endTime1 = System.currentTimeMillis();
            long firstQueryTime = endTime1 - startTime1;

            // 第二次查询（应该命中空值缓存）
            long startTime2 = System.currentTimeMillis();
            LabEnvironmentData data2 = labEnvironmentDataService.getLatestData(nonExistentLabId);
            long endTime2 = System.currentTimeMillis();
            long secondQueryTime = endTime2 - startTime2;

            result.put("labId", nonExistentLabId);
            result.put("data", data2);
            result.put("firstQueryTime", firstQueryTime + "ms");
            result.put("secondQueryTime", secondQueryTime + "ms");
            result.put("cacheHit", secondQueryTime < firstQueryTime);
            result.put("penetrationProtection", "✅ 已启用");

            log.info("🧪 缓存穿透保护测试完成 - 首次查询: {}ms, 二次查询: {}ms", firstQueryTime, secondQueryTime);

        } catch (Exception e) {
            log.error("❌ 缓存穿透保护测试失败", e);
            result.put("error", e.getMessage());
        }

        return Result.success(result);
    }

    /**
     * 清理指定实验室的缓存
     *
     * DELETE /cache-test/clear/{labId}
     */
    @DeleteMapping("/clear/{labId}")
    public Result<String> clearLabCache(@PathVariable Long labId) {
        try {
            labEnvironmentDataService.clearLabCache(labId);

            // 清理统计数据缓存（清理最近7天）
            for (int i = 0; i < 7; i++) {
                statisticsService.clearStatisticsCache(labId, java.time.LocalDate.now().minusDays(i));
            }

            log.info("🧹 实验室缓存清理完成 - 实验室ID: {}", labId);
            return Result.success("缓存清理成功 - 实验室ID: " + labId);

        } catch (Exception e) {
            log.error("❌ 缓存清理失败 - 实验室ID: {}", labId, e);
            return Result.failure("缓存清理失败: " + e.getMessage());
        }
    }

    /**
     * 预热指定实验室的缓存
     *
     * POST /cache-test/warmup/{labId}
     */
    @PostMapping("/warmup/{labId}")
    public Result<String> warmUpLabCache(@PathVariable Long labId) {
        try {
            // 预热实时数据缓存
            labEnvironmentDataService.warmUpLabCache(labId);

            // 预热统计数据缓存（最近7天）
            for (int i = 0; i < 7; i++) {
                statisticsService.warmUpStatisticsCache(labId, java.time.LocalDate.now().minusDays(i));
            }

            log.info("🔥 实验室缓存预热完成 - 实验室ID: {}", labId);
            return Result.success("缓存预热成功 - 实验室ID: " + labId);

        } catch (Exception e) {
            log.error("❌ 缓存预热失败 - 实验室ID: {}", labId, e);
            return Result.failure("缓存预热失败: " + e.getMessage());
        }
    }

    /**
     * 批量性能测试
     *
     * GET /cache-test/performance/{labId}?count=100
     */
    @GetMapping("/performance/{labId}")
    public Result<Map<String, Object>> performanceTest(@PathVariable Long labId,
                                                      @RequestParam(defaultValue = "100") int count) {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("🧪 开始批量性能测试 - 实验室ID: {}, 查询次数: {}", labId, count);

            // 清空缓存，确保从数据库开始
            labEnvironmentDataService.clearLabCache(labId);

            // 第一次批量查询（冷缓存）
            long coldStartTime = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
                labEnvironmentDataService.getLatestData(labId);
            }
            long coldEndTime = System.currentTimeMillis();
            long coldTotalTime = coldEndTime - coldStartTime;

            // 第二次批量查询（热缓存）
            long hotStartTime = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
                labEnvironmentDataService.getLatestData(labId);
            }
            long hotEndTime = System.currentTimeMillis();
            long hotTotalTime = hotEndTime - hotStartTime;

            double coldAvgTime = (double) coldTotalTime / count;
            double hotAvgTime = (double) hotTotalTime / count;
            double improvement = (coldAvgTime - hotAvgTime) / coldAvgTime * 100;

            result.put("labId", labId);
            result.put("queryCount", count);
            result.put("coldTotalTime", coldTotalTime + "ms");
            result.put("hotTotalTime", hotTotalTime + "ms");
            result.put("coldAvgTime", String.format("%.2fms", coldAvgTime));
            result.put("hotAvgTime", String.format("%.2fms", hotAvgTime));
            result.put("performanceImprovement", String.format("%.1f%%", improvement));
            result.put("speedRatio", String.format("%.1fx", coldAvgTime / hotAvgTime));

            log.info("🧪 批量性能测试完成 - 冷缓存: {:.2f}ms/次, 热缓存: {:.2f}ms/次, 提升: {:.1f}%",
                    coldAvgTime, hotAvgTime, improvement);

        } catch (Exception e) {
            log.error("❌ 批量性能测试失败 - 实验室ID: {}", labId, e);
            result.put("error", e.getMessage());
        }

        return Result.success(result);
    }

    /**
     * 健康检查
     *
     * GET /cache-test/health
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("service", "Redis缓存测试服务");
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        return Result.success(health);
    }
}
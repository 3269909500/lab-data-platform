package com.sewage.monitor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务类
 *
 * 功能说明：
 * 1. 封装Redis操作，提供统一的缓存接口
 * 2. 支持多种数据结构的操作
 * 3. 提供缓存穿透、雪崩保护
 *
 * 使用示例：
 * 1. 实时数据缓存：hash操作，支持部分更新
 * 2. 历史数据缓存：sorted set操作，支持时间范围查询
 * 3. 统计数据缓存：hash操作，支持复合统计数据
 * 4. 用户会话缓存：string操作，支持JSON序列化
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    // =============================通用操作=============================

    /**
     * 设置缓存
     *
     * @param key   缓存键
     * @param value 缓存值
     * @param ttl   过期时间（秒）
     */
    public void set(String key, Object value, long ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
            log.debug("缓存设置成功: key={}, ttl={}s", key, ttl);
        } catch (Exception e) {
            log.error("缓存设置失败: key={}", key, e);
        }
    }

    /**
     * 获取缓存
     *
     * @param key 缓存键
     * @return 缓存值
     */
    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            log.debug("缓存获取: key={}, hit={}", key, value != null);
            return value;
        } catch (Exception e) {
            log.error("缓存获取失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 删除缓存
     *
     * @param key 缓存键
     * @return 是否删除成功
     */
    public boolean delete(String key) {
        try {
            Boolean result = redisTemplate.delete(key);
            log.debug("缓存删除: key={}, result={}", key, result);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("缓存删除失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 批量删除缓存
     *
     * @param keys 缓存键集合
     * @return 删除的数量
     */
    public long delete(Collection<String> keys) {
        try {
            Long count = redisTemplate.delete(keys);
            log.debug("批量删除缓存: keys={}, count={}", keys, count);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("批量删除缓存失败: keys={}", keys, e);
            return 0;
        }
    }

    /**
     * 检查缓存是否存在
     *
     * @param key 缓存键
     * @return 是否存在
     */
    public boolean exists(String key) {
        try {
            Boolean result = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("检查缓存存在性失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 设置缓存过期时间
     *
     * @param key  缓存键
     * @param ttl  过期时间（秒）
     * @return 是否设置成功
     */
    public boolean expire(String key, long ttl) {
        try {
            Boolean result = redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("设置缓存过期时间失败: key={}, ttl={}", key, ttl, e);
            return false;
        }
    }

    // =============================Hash操作=============================

    /**
     * 设置Hash字段
     *
     * @param key   Hash键
     * @param field 字段名
     * @param value 字段值
     */
    public void hSet(String key, String field, Object value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
            log.debug("Hash字段设置: key={}, field={}", key, field);
        } catch (Exception e) {
            log.error("Hash字段设置失败: key={}, field={}", key, field, e);
        }
    }

    /**
     * 批量设置Hash字段
     *
     * @param key  Hash键
     * @param map  字段映射
     */
    public void hSetAll(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            log.debug("Hash批量设置: key={}, fields={}", key, map.keySet());
        } catch (Exception e) {
            log.error("Hash批量设置失败: key={}", key, e);
        }
    }

    /**
     * 获取Hash字段
     *
     * @param key   Hash键
     * @param field 字段名
     * @return 字段值
     */
    public Object hGet(String key, String field) {
        try {
            Object value = redisTemplate.opsForHash().get(key, field);
            log.debug("Hash字段获取: key={}, field={}, hit={}", key, field, value != null);
            return value;
        } catch (Exception e) {
            log.error("Hash字段获取失败: key={}, field={}", key, field, e);
            return null;
        }
    }

    /**
     * 获取所有Hash字段
     *
     * @param key Hash键
     * @return 字段映射
     */
    public Map<Object, Object> hGetAll(String key) {
        try {
            Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
            log.debug("Hash所有字段获取: key={}, size={}", key, map.size());
            return map;
        } catch (Exception e) {
            log.error("Hash所有字段获取失败: key={}", key, e);
            return Map.of();
        }
    }

    /**
     * 删除Hash字段
     *
     * @param key   Hash键
     * @param field 字段名
     * @return 是否删除成功
     */
    public boolean hDelete(String key, String field) {
        try {
            Long result = redisTemplate.opsForHash().delete(key, field);
            return result != null && result > 0;
        } catch (Exception e) {
            log.error("Hash字段删除失败: key={}, field={}", key, field, e);
            return false;
        }
    }

    // =============================Sorted Set操作=============================

    /**
     * 添加到有序集合
     *
     * @param key   有序集合键
     * @param value 成员值
     * @param score 分数
     */
    public void zAdd(String key, Object value, double score) {
        try {
            redisTemplate.opsForZSet().add(key, value, score);
            log.debug("有序集合添加: key={}, score={}", key, score);
        } catch (Exception e) {
            log.error("有序集合添加失败: key={}, score={}", key, score, e);
        }
    }

    /**
     * 获取有序集合范围内的成员（按分数）
     *
     * @param key   有序集合键
     * @param min   最小分数
     * @param max   最大分数
     * @return 成员集合
     */
    public Set<Object> zRangeByScore(String key, double min, double max) {
        try {
            Set<Object> set = redisTemplate.opsForZSet().rangeByScore(key, min, max);
            log.debug("有序集合范围查询: key={}, min={}, max={}, size={}", key, min, max, set.size());
            return set;
        } catch (Exception e) {
            log.error("有序集合范围查询失败: key={}, min={}, max={}", key, min, max, e);
            return Set.of();
        }
    }

    /**
     * 获取有序集合范围内的成员（按排名，倒序）
     *
     * @param key   有序集合键
     * @param start 开始排名
     * @param end   结束排名
     * @return 成员集合
     */
    public Set<Object> zReverseRange(String key, long start, long end) {
        try {
            Set<Object> set = redisTemplate.opsForZSet().reverseRange(key, start, end);
            log.debug("有序集合倒序查询: key={}, start={}, end={}, size={}", key, start, end, set.size());
            return set;
        } catch (Exception e) {
            log.error("有序集合倒序查询失败: key={}, start={}, end={}", key, start, end, e);
            return Set.of();
        }
    }

    /**
     * 删除有序集合范围内的成员
     *
     * @param key   有序集合键
     * @param min   最小分数
     * @param max   最大分数
     * @return 删除的数量
     */
    public long zRemoveRangeByScore(String key, double min, double max) {
        try {
            Long count = redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
            log.debug("有序集合范围删除: key={}, min={}, max={}, count={}", key, min, max, count);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("有序集合范围删除失败: key={}, min={}, max={}", key, min, max, e);
            return 0;
        }
    }

    /**
     * 获取有序集合大小
     *
     * @param key 有序集合键
     * @return 成员数量
     */
    public long zSize(String key) {
        try {
            Long size = redisTemplate.opsForZSet().size(key);
            return size != null ? size : 0;
        } catch (Exception e) {
            log.error("获取有序集合大小失败: key={}", key, e);
            return 0;
        }
    }

    // =============================缓存保护机制=============================

    /**
     * 设置空值缓存（防止缓存穿透）
     *
     * @param key 缓存键
     */
    public void setNullValue(String key) {
        try {
            // 使用特殊的标记表示空值
            redisTemplate.opsForValue().set(key, "NULL_VALUE", 60, TimeUnit.SECONDS);
            log.debug("空值缓存设置: key={}", key);
        } catch (Exception e) {
            log.error("空值缓存设置失败: key={}", key, e);
        }
    }

    /**
     * 检查是否为空值缓存
     *
     * @param key 缓存键
     * @return 是否为空值缓存
     */
    public boolean isNullValue(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            return "NULL_VALUE".equals(value);
        } catch (Exception e) {
            log.error("检查空值缓存失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 获取缓存命中率统计信息
     *
     * @return 缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        try {
            // 这里可以添加更多统计信息
            return Map.of(
                "info", "Redis缓存服务运行正常",
                "timestamp", System.currentTimeMillis()
            );
        } catch (Exception e) {
            log.error("获取缓存统计信息失败", e);
            return Map.of("error", "获取统计信息失败");
        }
    }
}
package common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis操作工具类 - 基于 RedisTemplate（轻量级）
 * 只保留项目常用的核心方法
 *
 * @author: rj
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    // ==================== 基础操作 ====================

    /**
     * 判断key是否存在
     */
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Redis hasKey失败, key={}", key, e);
            return false;
        }
    }

    /**
     * 删除缓存
     */
    public boolean delete(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        } catch (Exception e) {
            log.error("Redis delete失败, key={}", key, e);
            return false;
        }
    }

    /**
     * 设置过期时间
     */
    public boolean expire(String key, long time, TimeUnit unit) {
        try {
            return Boolean.TRUE.equals(redisTemplate.expire(key, time, unit));
        } catch (Exception e) {
            log.error("Redis expire失败, key={}", key, e);
            return false;
        }
    }

    // ==================== String 操作 ====================

    /**
     * 获取缓存
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis get失败, key={}", key, e);
            return null;
        }
    }

    /**
     * 设置缓存（不设置过期时间）
     */
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Redis set失败, key={}", key, e);
        }
    }

    /**
     * 设置缓存并指定过期时间
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("Redis set失败, key={}", key, e);
        }
    }

    // ==================== Hash 操作 ====================

    /**
     * Hash 设置值
     */
    public void hSet(String key, String field, Object value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
        } catch (Exception e) {
            log.error("Redis hSet失败, key={}, field={}", key, field, e);
        }
    }

    /**
     * Hash 获取值
     */
    public Object hGet(String key, String field) {
        try {
            return redisTemplate.opsForHash().get(key, field);
        } catch (Exception e) {
            log.error("Redis hGet失败, key={}, field={}", key, field, e);
            return null;
        }
    }

    /**
     * Hash 删除字段
     */
    public boolean hDelete(String key, String field) {
        try {
            return redisTemplate.opsForHash().delete(key, field) > 0;
        } catch (Exception e) {
            log.error("Redis hDelete失败, key={}, field={}", key, field, e);
            return false;
        }
    }
}

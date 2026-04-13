package common.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis操作工具类
 *
 * @author: rj
 */
@Component
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ==================== 通用操作 ====================

    /**
     * 设置过期时间
     *
     * @param key     键
     * @param time    时间
     * @param timeUnit 时间单位
     * @return true-成功，false-失败
     */
    public boolean expire(String key, long time, TimeUnit timeUnit) {
        if (time > 0) {
            return Boolean.TRUE.equals(redisTemplate.expire(key, time, timeUnit));
        }
        return false;
    }

    /**
     * 获取过期时间
     *
     * @param key 键
     * @return 过期时间（秒），-1表示永不过期，-2表示键不存在
     */
    public long getExpire(String key) {
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire != null ? expire : -2;
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true-存在，false-不存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 删除缓存
     *
     * @param key 键（可变参数，可传多个）
     * @return 删除的数量
     */
    public long delete(String... key) {
        if (key != null && key.length > 0) {
            Long count = redisTemplate.delete(List.of(key));
            return count != null ? count : 0;
        }
        return 0;
    }

    // ==================== String 操作 ====================

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true-成功，false-失败
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间（秒）
     * @return true-成功，false-失败
     */
    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间（自定义时间单位）
     *
     * @param key      键
     * @param value    值
     * @param time     时间
     * @param timeUnit 时间单位
     * @return true-成功，false-失败
     */
    public boolean set(String key, Object value, long time, TimeUnit timeUnit) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, timeUnit);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 递增
     *
     * @param key   键
     * @param delta 要增加几（大于0）
     * @return 增加后的值
     */
    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        Long result = redisTemplate.opsForValue().increment(key, delta);
        return result != null ? result : 0;
    }

    /**
     * 递减
     *
     * @param key   键
     * @param delta 要减少几（大于0）
     * @return 减少后的值
     */
    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        Long result = redisTemplate.opsForValue().increment(key, -delta);
        return result != null ? result : 0;
    }

    // ==================== Hash 操作 ====================

    /**
     * HashGet
     *
     * @param key  键（不能为null）
     * @param item 项（不能为null）
     * @return 值
     */
    public Object hget(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 获取hashKey对应的所有键值
     *
     * @param key 键
     * @return 对应的多个键值
     */
    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * HashSet
     *
     * @param key 键
     * @param map 对应多个键值
     * @return true-成功，false-失败
     */
    public boolean hmset(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * HashSet 并设置时间
     *
     * @param key  键
     * @param map  对应多个键值
     * @param time 时间（秒）
     * @return true-成功，false-失败
     */
    public boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据，如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @return true-成功，false-失败
     */
    public boolean hset(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据，如果不存在将创建，并设置时间
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @param time  时间（秒）
     * @return true-成功，false-失败
     */
    public boolean hset(String key, String item, Object value, long time) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 删除hash表中的值
     *
     * @param key  键（不能为null）
     * @param item 项（可以使多个，不能为null）
     */
    public void hdel(String key, Object... item) {
        redisTemplate.opsForHash().delete(key, item);
    }

    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键（不能为null）
     * @param item 项（不能为null）
     * @return true-存在，false-不存在
     */
    public boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * hash递增 如果不存在，就会创建一个 并把新增后的值返回
     *
     * @param key  键
     * @param item 项
     * @param by   要增加几（大于0）
     * @return 增加后的值
     */
    public double hincr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }

    /**
     * hash递减
     *
     * @param key  键
     * @param item 项
     * @param by   要减少几（小于0）
     * @return 减少后的值
     */
    public double hdecr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, -by);
    }

    // ==================== Set 操作 ====================

    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     * @return Set集合
     */
    public Set<Object> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据value从一个set中查询，是否存在
     *
     * @param key   键
     * @param value 值
     * @return true-存在，false-不存在
     */
    public boolean sHasKey(String key, Object value) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 将数据放入set缓存
     *
     * @param key    键
     * @param values 值（可以是多个）
     * @return 成功个数
     */
    public long sSet(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 将set数据放入缓存并设置时间
     *
     * @param key    键
     * @param time   时间（秒）
     * @param values 值（可以是多个）
     * @return 成功个数
     */
    public long sSetAndTime(String key, long time, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0) {
                expire(key, time, TimeUnit.SECONDS);
            }
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取set缓存的长度
     *
     * @param key 键
     * @return 长度
     */
    public long sGetSetSize(String key) {
        try {
            Long size = redisTemplate.opsForSet().size(key);
            return size != null ? size : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 移除值为value的
     *
     * @param key    键
     * @param values 值（可以是多个）
     * @return 移除的个数
     */
    public long setRemove(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().remove(key, values);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    // ==================== List 操作 ====================

    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束（0到-1代表所有值）
     * @return List集合
     */
    public List<Object> lGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取list缓存的长度
     *
     * @param key 键
     * @return 长度
     */
    public long lGetListSize(String key) {
        try {
            Long size = redisTemplate.opsForList().size(key);
            return size != null ? size : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 通过索引获取list中的值
     *
     * @param key   键
     * @param index 索引（index>=0时，0表头，1第二个元素，依次类推；index<0时，-1表尾，-2倒数第二个元素，依次类推）
     * @return 值
     */
    public Object lGetIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return true-成功，false-失败
     */
    public boolean lSet(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 将list放入缓存并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间（秒）
     * @return true-成功，false-失败
     */
    public boolean lSet(String key, Object value, long time) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0) {
                expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 将list放入缓存（批量）
     *
     * @param key   键
     * @param value 值（集合）
     * @return true-成功，false-失败
     */
    public boolean lSet(String key, Collection<Object> value) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 将list放入缓存并设置时间（批量）
     *
     * @param key   键
     * @param value 值（集合）
     * @param time  时间（秒）
     * @return true-成功，false-失败
     */
    public boolean lSet(String key, Collection<Object> value, long time) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0) {
                expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 根据索引修改list中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     * @return true-成功，false-失败
     */
    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 移除N个值为value的元素
     *
     * @param key   键
     * @param count 移除多少个（count>0：从表头开始移除；count<0：从表尾开始移除；count=0：移除所有）
     * @param value 值
     * @return 移除的个数
     */
    public long lRemove(String key, long count, Object value) {
        try {
            Long remove = redisTemplate.opsForList().remove(key, count, value);
            return remove != null ? remove : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    // ==================== ZSet 操作 ====================

    /**
     * 添加元素到有序集合
     *
     * @param key   键
     * @param value 值
     * @param score 分数
     * @return true-成功，false-失败
     */
    public boolean zSet(String key, Object value, double score) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForZSet().add(key, value, score));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取有序集合中指定范围的元素（从小到大排序）
     *
     * @param key   键
     * @param start 开始位置
     * @param end   结束位置
     * @return Set集合
     */
    public Set<Object> zGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForZSet().range(key, start, end);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取有序集合中指定分数范围的元素
     *
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return Set集合
     */
    public Set<Object> zGetByScore(String key, double min, double max) {
        try {
            return redisTemplate.opsForZSet().rangeByScore(key, min, max);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取有序集合的大小
     *
     * @param key 键
     * @return 大小
     */
    public long zGetSize(String key) {
        try {
            Long size = redisTemplate.opsForZSet().size(key);
            return size != null ? size : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取元素的分数
     *
     * @param key   键
     * @param value 值
     * @return 分数
     */
    public Double zGetScore(String key, Object value) {
        try {
            return redisTemplate.opsForZSet().score(key, value);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 移除有序集合中的元素
     *
     * @param key    键
     * @param values 值（可以是多个）
     * @return 移除的个数
     */
    public long zRemove(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForZSet().remove(key, values);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    // ==================== 分布式锁操作 ====================

    private static final Long RELEASE_SUCCESS = 1L;

    private static final String UNLOCK_LUA =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "   return redis.call('del', KEYS[1]) " +
                    "else " +
                    "   return 0 " +
                    "end";

    public boolean tryLock(String lockKey, String requestId, long expireTime, TimeUnit timeUnit) {
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(
                    lockKey,
                    requestId,
                    expireTime,
                    timeUnit
            );
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean tryLock(String lockKey, String requestId, long expireTimeMs) {
        return tryLock(lockKey, requestId, expireTimeMs, TimeUnit.MILLISECONDS);
    }

    public boolean releaseLock(String lockKey, String requestId) {
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_LUA, Long.class);
            Long result = redisTemplate.execute(script, Collections.singletonList(lockKey), requestId);
            return RELEASE_SUCCESS.equals(result);
        } catch (Exception e) {
            return false;
        }
    }

    public String generateLockId() {
        return Thread.currentThread().getId() + ":" + System.currentTimeMillis();
    }

}

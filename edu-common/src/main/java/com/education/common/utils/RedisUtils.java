package com.education.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 * 
 * 功能清单：
 * 1. 字符串操作：set/get/delete/expire
 * 2. Hash操作：hSet/hGet/hGetAll/hDelete
 * 3. Set操作：sAdd/sMembers/sIsMember/sRemove
 * 4. List操作：lPush/rPush/lPop/rPop/lRange
 * 5. ZSet操作：zAdd/zRange/zScore/zRemove
 * 6. 分布式锁：tryLock/unlock
 * 7. 限流器：rateLimit
 * 8. 缓存穿透防护：bloomFilter
 */
@Component
public class RedisUtils {

    private static StringRedisTemplate stringRedisTemplate;
    private static RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        RedisUtils.stringRedisTemplate = stringRedisTemplate;
    }

    @Autowired
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        RedisUtils.redisTemplate = redisTemplate;
    }

    // ==================== 字符串操作 ====================

    /**
     * 设置字符串缓存
     */
    public static void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置字符串缓存并指定过期时间
     */
    public static void set(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取字符串缓存
     */
    public static String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存
     */
    public static Boolean delete(String key) {
        return stringRedisTemplate.delete(key);
    }

    /**
     * 批量删除缓存
     */
    public static Long delete(Collection<String> keys) {
        return stringRedisTemplate.delete(keys);
    }

    /**
     * 检查键是否存在
     */
    public static Boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    /**
     * 设置过期时间
     */
    public static Boolean expire(String key, long timeout, TimeUnit unit) {
        return stringRedisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取过期时间（秒）
     */
    public static Long getExpire(String key) {
        return stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 递增
     */
    public static Long increment(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }

    /**
     * 递减
     */
    public static Long decrement(String key) {
        return stringRedisTemplate.opsForValue().decrement(key);
    }

    // ==================== Hash操作 ====================

    /**
     * Hash设置
     */
    public static void hSet(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * Hash获取
     */
    public static Object hGet(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    /**
     * Hash获取所有
     */
    public static Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * Hash删除
     */
    public static Long hDelete(String key, Object... hashKeys) {
        return redisTemplate.opsForHash().delete(key, hashKeys);
    }

    /**
     * Hash检查字段是否存在
     */
    public static Boolean hHasKey(String key, String hashKey) {
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    // ==================== Set操作 ====================

    /**
     * Set添加
     */
    public static Long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * Set获取所有成员
     */
    public static Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * Set检查是否包含
     */
    public static Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * Set删除
     */
    public static Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    /**
     * Set获取大小
     */
    public static Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    // ==================== List操作 ====================

    /**
     * List左推入
     */
    public static Long lPush(String key, Object... values) {
        return redisTemplate.opsForList().leftPushAll(key, values);
    }

    /**
     * List右推入
     */
    public static Long rPush(String key, Object... values) {
        return redisTemplate.opsForList().rightPushAll(key, values);
    }

    /**
     * List左弹出
     */
    public static Object lPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    /**
     * List右弹出
     */
    public static Object rPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    /**
     * List获取范围
     */
    public static List<Object> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * List获取大小
     */
    public static Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    // ==================== ZSet操作 ====================

    /**
     * ZSet添加
     */
    public static Boolean zAdd(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * ZSet获取排名范围
     */
    public static Set<Object> zRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * ZSet获取分数
     */
    public static Double zScore(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    /**
     * ZSet删除
     */
    public static Long zRemove(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }

    // ==================== 分布式锁 ====================

    /**
     * 尝试获取分布式锁
     * 
     * @param lockKey 锁key
     * @param requestId 请求标识（用于释放锁时验证）
     * @param expireTime 过期时间（秒）
     * @return 是否获取成功
     */
    public static Boolean tryLock(String lockKey, String requestId, long expireTime) {
        return stringRedisTemplate.opsForValue().setIfAbsent(lockKey, requestId, expireTime, TimeUnit.SECONDS);
    }

    /**
     * 释放分布式锁
     * 
     * @param lockKey 锁key
     * @param requestId 请求标识
     * @return 是否释放成功
     */
    public static Boolean unlock(String lockKey, String requestId) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long result = stringRedisTemplate.execute(
                new org.springframework.data.redis.core.script.DefaultRedisScript<>(script, Long.class),
                java.util.Collections.singletonList(lockKey),
                requestId
        );
        return result != null && result > 0;
    }

    // ==================== 限流器 ====================

    /**
     * 滑动窗口限流
     * 
     * @param key 限流key
     * @param limit 限制次数
     * @param windowSeconds 窗口时间（秒）
     * @return 是否允许通过
     */
    public static Boolean rateLimit(String key, int limit, long windowSeconds) {
        String script = 
            "local key = KEYS[1] " +
            "local limit = tonumber(ARGV[1]) " +
            "local window = tonumber(ARGV[2]) " +
            "local current = redis.call('get', key) " +
            "if current and tonumber(current) >= limit then " +
            "  return 0 " +
            "end " +
            "current = redis.call('incr', key) " +
            "if tonumber(current) == 1 then " +
            "  redis.call('expire', key, window) " +
            "end " +
            "return 1";
        
        Long result = stringRedisTemplate.execute(
                new org.springframework.data.redis.core.script.DefaultRedisScript<>(script, Long.class),
                java.util.Collections.singletonList(key),
                String.valueOf(limit),
                String.valueOf(windowSeconds)
        );
        return result != null && result > 0;
    }

    // ==================== 在线用户管理 ====================

    /**
     * 用户上线
     */
    public static void userOnline(Long userId, String sessionId) {
        stringRedisTemplate.opsForValue().set("online:user:" + userId, sessionId, 30, TimeUnit.MINUTES);
        stringRedisTemplate.opsForSet().add("online:users", String.valueOf(userId));
    }

    /**
     * 用户下线
     */
    public static void userOffline(Long userId) {
        stringRedisTemplate.delete("online:user:" + userId);
        stringRedisTemplate.opsForSet().remove("online:users", String.valueOf(userId));
    }

    /**
     * 检查用户是否在线
     */
    public static Boolean isUserOnline(Long userId) {
        return stringRedisTemplate.hasKey("online:user:" + userId);
    }

    /**
     * 获取在线用户列表
     */
    public static Set<String> getOnlineUsers() {
        return stringRedisTemplate.opsForSet().members("online:users");
    }

    // ==================== 验证码管理 ====================

    /**
     * 存储验证码
     */
    public static void setVerifyCode(String phone, String code) {
        stringRedisTemplate.opsForValue().set("verify:code:" + phone, code, 5, TimeUnit.MINUTES);
    }

    /**
     * 获取验证码
     */
    public static String getVerifyCode(String phone) {
        return stringRedisTemplate.opsForValue().get("verify:code:" + phone);
    }

    /**
     * 删除验证码
     */
    public static void deleteVerifyCode(String phone) {
        stringRedisTemplate.delete("verify:code:" + phone);
    }

    // ==================== 缓存穿透防护 ====================

    /**
     * 缓存空值（防止缓存穿透）
     */
    public static void setEmptyCache(String key, long expireSeconds) {
        stringRedisTemplate.opsForValue().set(key, "__EMPTY__", expireSeconds, TimeUnit.SECONDS);
    }

    /**
     * 检查是否为空值缓存
     */
    public static Boolean isEmptyCache(String key) {
        String value = stringRedisTemplate.opsForValue().get(key);
        return "__EMPTY__".equals(value);
    }
}

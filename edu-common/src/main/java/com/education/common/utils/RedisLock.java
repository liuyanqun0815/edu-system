package com.education.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis分布式锁工具类
 * 
 * 使用示例：
 * <pre>
 * // 简单用法
 * RedisLock.lock("order:create:" + orderId, () -> {
 *     // 业务逻辑
 *     return result;
 * });
 * 
 * // 带超时控制
 * RedisLock.tryLock("order:create:" + orderId, 10, () -> {
 *     // 业务逻辑
 *     return result;
 * });
 * </pre>
 */
public class RedisLock {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisLock.class);

    /**
     * 获取分布式锁（阻塞等待）
     * 
     * @param lockKey 锁key
     * @param action 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     */
    public static <T> T lock(String lockKey, Supplier<T> action) {
        return lock(lockKey, 30, action);
    }

    /**
     * 获取分布式锁（带超时时间）
     * 
     * @param lockKey 锁key
     * @param expireSeconds 锁超时时间（秒）
     * @param action 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     */
    public static <T> T lock(String lockKey, long expireSeconds, Supplier<T> action) {
        String requestId = UUID.randomUUID().toString();
        boolean locked = false;
        
        try {
            // 尝试获取锁
            locked = RedisUtils.tryLock(lockKey, requestId, expireSeconds);
            
            if (!locked) {
                // 获取锁失败，短暂等待后重试
                sleep(100);
                locked = RedisUtils.tryLock(lockKey, requestId, expireSeconds);
                
                if (!locked) {
                    throw new RuntimeException("获取分布式锁失败: " + lockKey);
                }
            }
            
            logger.debug("获取分布式锁成功: {}", lockKey);
            return action.get();
            
        } finally {
            // 释放锁
            if (locked) {
                RedisUtils.unlock(lockKey, requestId);
                logger.debug("释放分布式锁: {}", lockKey);
            }
        }
    }

    /**
     * 尝试获取分布式锁（非阻塞）
     * 
     * @param lockKey 锁key
     * @param expireSeconds 锁超时时间（秒）
     * @param action 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果，如果获取锁失败返回null
     */
    public static <T> T tryLock(String lockKey, long expireSeconds, Supplier<T> action) {
        String requestId = UUID.randomUUID().toString();
        boolean locked = false;
        
        try {
            locked = RedisUtils.tryLock(lockKey, requestId, expireSeconds);
            
            if (!locked) {
                logger.warn("获取分布式锁失败（非阻塞）: {}", lockKey);
                return null;
            }
            
            logger.debug("获取分布式锁成功（非阻塞）: {}", lockKey);
            return action.get();
            
        } finally {
            if (locked) {
                RedisUtils.unlock(lockKey, requestId);
            }
        }
    }

    /**
     * 无返回值的锁操作
     * 
     * @param lockKey 锁key
     * @param action 要执行的操作
     */
    public static void lock(String lockKey, Runnable action) {
        lock(lockKey, () -> {
            action.run();
            return null;
        });
    }

    /**
     * 无返回值的锁操作（带超时）
     * 
     * @param lockKey 锁key
     * @param expireSeconds 锁超时时间（秒）
     * @param action 要执行的操作
     */
    public static void lock(String lockKey, long expireSeconds, Runnable action) {
        lock(lockKey, expireSeconds, () -> {
            action.run();
            return null;
        });
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

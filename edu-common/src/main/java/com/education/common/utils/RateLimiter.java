package com.education.common.utils;

import com.education.common.constants.RedisKeyConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Redis限流器
 * 
 * 使用场景：
 * 1. 接口限流：防止恶意刷接口
 * 2. 短信验证码限流：防止短信轰炸
 * 3. 登录失败限流：防止暴力破解
 * 
 * 使用示例：
 * <pre>
 * // 1分钟内最多发送5次验证码
 * if (!RateLimiter.allow("sms:" + phone, 5, 60)) {
 *     throw new BusinessException("操作过于频繁，请稍后再试");
 * }
 * 
 * // 1小时内最多登录失败3次
 * if (!RateLimiter.allow("login:fail:" + username, 3, 3600)) {
 *     throw new BusinessException("登录失败次数过多，请1小时后再试");
 * }
 * </pre>
 */
public class RateLimiter {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimiter.class);

    /**
     * 检查是否允许操作
     * 
     * @param key 限流key
     * @param limit 限制次数
     * @param windowSeconds 窗口时间（秒）
     * @return 是否允许
     */
    public static Boolean allow(String key, int limit, long windowSeconds) {
        boolean allowed = RedisUtils.rateLimit(key, limit, windowSeconds);
        
        if (!allowed) {
            logger.warn("限流拦截: key={}, limit={}, window={}s", key, limit, windowSeconds);
        }
        
        return allowed;
    }

    /**
     * 检查并抛出异常（不允许时）
     * 
     * @param key 限流key
     * @param limit 限制次数
     * @param windowSeconds 窗口时间（秒）
     * @param message 异常消息
     * @throws RuntimeException 当触发限流时抛出
     */
    public static void checkAndThrow(String key, int limit, long windowSeconds, String message) {
        if (!allow(key, limit, windowSeconds)) {
            throw new RuntimeException(message);
        }
    }

    /**
     * 短信验证码限流(5分钟最多5次)
     */
    public static boolean allowSmsCode(String phone) {
        return allow(RedisKeyConstants.RATE_SMS + phone, 5, 300);
    }

    /**
     * 登录失败限流(1小时最多3次)
     */
    public static boolean allowLoginFail(String username) {
        return allow(RedisKeyConstants.RATE_LOGIN_FAIL + username, 3, 3600);
    }

    /**
     * 接口调用限流(1分钟最多60次)
     */
    public static boolean allowApiCall(String userId) {
        return allow(RedisKeyConstants.RATE_API + userId, 60, 60);
    }

    /**
     * 文件上传限流(1小时最多10次)
     */
    public static boolean allowFileUpload(String userId) {
        return allow(RedisKeyConstants.RATE_UPLOAD + userId, 10, 3600);
    }

    /**
     * 获取剩余次数
     * 
     * @param key 限流key
     * @param limit 限制次数
     * @return 剩余次数
     */
    public static Long getRemaining(String key, int limit) {
        String count = RedisUtils.get(key);
        if (count == null) {
            return (long) limit;
        }
        long current = Long.parseLong(count);
        return Math.max(0, limit - current);
    }
}

package com.education.common.constants;

/**
 * Redis Key前缀常量
 * 
 * @author system
 * @since 2026-04-09
 */
public final class RedisKeyConstants {
    
    /** Token黑名单前缀 */
    public static final String TOKEN_BLACKLIST = "token:blacklist:";
    
    /** 登录验证码前缀 */
    public static final String CAPTCHA_LOGIN = "captcha:login:";
    
    /** 找回密码验证码前缀 */
    public static final String CAPTCHA_FORGOT = "captcha:forgot:";
    
    /** 短信限流前缀 */
    public static final String RATE_SMS = "rate:sms:";
    
    /** 登录失败限流前缀 */
    public static final String RATE_LOGIN_FAIL = "rate:login:fail:";
    
    /** API限流前缀 */
    public static final String RATE_API = "rate:api:";
    
    /** 上传限流前缀 */
    public static final String RATE_UPLOAD = "rate:upload:";
    
    /** 用户信息缓存前缀 */
    public static final String USER_INFO = "user:info:";
    
    /** 菜单缓存前缀 */
    public static final String MENU_CACHE = "menu:cache:";
    
    private RedisKeyConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

package com.education.common.utils;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * JWT工具类静态代理
 * 
 * <p>为兼容旧代码,提供静态方法调用,内部委托给Spring管理的JwtUtils实例</p>
 */
@Component
public class JwtUtilsProxy {

    private static JwtUtils jwtUtils;

    @Autowired
    private JwtUtils jwtUtilsInstance;

    @PostConstruct
    public void init() {
        jwtUtils = jwtUtilsInstance;
    }

    /**
     * 生成Token
     */
    public static String generateToken(Long userId, String username) {
        return jwtUtils.generateToken(userId, username);
    }

    /**
     * 生成刷新Token
     */
    public static String generateRefreshToken(Long userId, String username) {
        return jwtUtils.generateRefreshToken(userId, username);
    }

    /**
     * 从Token中获取Claims
     */
    public static Claims getClaimsFromToken(String token) {
        return jwtUtils.getClaimsFromToken(token);
    }

    /**
     * 从Token中获取用户ID
     */
    public static Long getUserIdFromToken(String token) {
        return jwtUtils.getUserIdFromToken(token);
    }

    /**
     * 从Token中获取用户名
     */
    public static String getUsernameFromToken(String token) {
        return jwtUtils.getUsernameFromToken(token);
    }

    /**
     * 验证Token是否有效
     */
    public static boolean validateToken(String token) {
        return jwtUtils.validateToken(token);
    }

    /**
     * 检查Token是否即将过期(剩余时间少于30分钟)
     */
    public static boolean isTokenExpiringSoon(String token) {
        return jwtUtils.isTokenExpiringSoon(token);
    }

    /**
     * 获取Token剩余有效时间(秒)
     */
    public static Long getExpireTime(String token) {
        return jwtUtils.getExpireTime(token);
    }

    /**
     * 刷新Token
     */
    public static String refreshToken(String token) {
        return jwtUtils.refreshToken(token);
    }
}

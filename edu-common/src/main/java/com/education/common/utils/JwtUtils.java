package com.education.common.utils;

import com.education.common.config.EduBusinessProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 
 * <p>支持从Nacos动态配置刷新JWT密钥和过期时间</p>
 */
@Component
@RefreshScope
public class JwtUtils {

    @Autowired
    private EduBusinessProperties properties;

    private static Key KEY;
    private static long STATIC_EXPIRATION_TIME;
    private static long STATIC_REFRESH_EXPIRATION_TIME;
    private static long STATIC_TOKEN_EXPIRING_SOON_THRESHOLD;

    @PostConstruct
    public void init() {
        EduBusinessProperties.JwtProperties jwt = properties.getJwt();
        KEY = Keys.hmacShaKeyFor(jwt.getSecret().getBytes());
        STATIC_EXPIRATION_TIME = jwt.getExpiration();
        STATIC_REFRESH_EXPIRATION_TIME = jwt.getRefreshExpiration();
        STATIC_TOKEN_EXPIRING_SOON_THRESHOLD = properties.getBusiness().getTokenExpiringSoonThreshold();
    }

    /**
     * 生成Token
     */
    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + STATIC_EXPIRATION_TIME))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成刷新Token
     */
    public String generateRefreshToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", "refresh");
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + STATIC_REFRESH_EXPIRATION_TIME))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从Token中获取Claims
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        Object userId = claims.get("userId");
        if (userId == null) {
            return null;
        }
        return Long.valueOf(userId.toString());
    }
    
    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getSubject() : null;
    }
    
    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) {
                return false;
            }
            // 检查是否过期
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查Token是否即将过期(剩余时间少于配置的阈值,默认30分钟)
     */
    public boolean isTokenExpiringSoon(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return true;
        }
        long remainingTime = claims.getExpiration().getTime() - System.currentTimeMillis();
        return remainingTime < STATIC_TOKEN_EXPIRING_SOON_THRESHOLD;
    }
    
    /**
     * 获取Token剩余有效时间(秒)
     */
    public Long getExpireTime(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        long remainingTime = claims.getExpiration().getTime() - System.currentTimeMillis();
        return remainingTime > 0 ? remainingTime / 1000 : 0;
    }
    
    /**
     * 刷新Token
     */
    public String refreshToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
            
        Long userId = getUserIdFromToken(token);
        String username = claims.getSubject();
            
        return generateToken(userId, username);
    }
}

package com.education.common.interceptor;

import com.education.common.config.EduBusinessProperties;
import com.education.common.exception.BusinessException;
import com.education.common.constants.RedisKeyConstants;
import com.education.common.utils.RateLimiter;
import com.education.common.utils.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * 防重复提交拦截器
 * 
 * 功能：
 * 1. 基于Redis实现接口级别的防重复提交
 * 2. 结合限流器防止恶意刷接口
 * 3. 支持从Nacos动态读取限流配置
 * 
 * 使用方式：
 * <pre>
 * // 1. 注册拦截器（WebMvcConfig）
 * registry.addInterceptor(repeatSubmitInterceptor)
 *         .addPathPatterns("/api/**")
 *         .excludePathPatterns("/api/public/**");
 * 
 * // 2. 自动生效，所有请求都会进行防重复校验
 * </pre>
 */
@Component
public class RepeatSubmitInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(RepeatSubmitInterceptor.class);
    
    @Autowired
    private EduBusinessProperties properties;
    
    /**
     * 防重复提交标识头
     */
    private static final String REPEAT_SUBMIT_HEADER = "X-Repeat-Submit-Token";
    
    /**
     * 防重复提交过期时间（秒）
     */
    private static final int REPEAT_SUBMIT_EXPIRE = 3;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只拦截POST/PUT/DELETE请求
        String method = request.getMethod();
        if (!"POST".equalsIgnoreCase(method) && !"PUT".equalsIgnoreCase(method) && !"DELETE".equalsIgnoreCase(method)) {
            return true;
        }
        
        try {
            // 1. 获取用户标识（从请求头或Session）
            String userIdentify = getUserIdentify(request);
            
            // 2. 获取请求URI
            String uri = request.getRequestURI();
            
            // 3. 获取请求参数（用于生成唯一标识）
            String paramKey = getRequestParams(request);
            
            // 4. 生成防重复提交key
            String repeatKey = String.format("repeat:submit:%s:%s:%s", userIdentify, uri, paramKey);
            
            // 5. 检查是否重复提交
            if (RedisUtils.hasKey(repeatKey)) {
                logger.warn("重复提交拦截: user={}, uri={}", userIdentify, uri);
                throw new BusinessException("操作过于频繁，请勿重复提交");
            }
            
            // 6. 设置防重复标识
            RedisUtils.set(repeatKey, UUID.randomUUID().toString(), REPEAT_SUBMIT_EXPIRE, java.util.concurrent.TimeUnit.SECONDS);
            
            // 7. 接口限流检查(从Nacos动态读取配置)
            String rateKey = String.format("%s%s", RedisKeyConstants.RATE_API, userIdentify);
            EduBusinessProperties.RateLimitProperties.RateLimitConfig apiLimit = 
                properties.getRateLimit().getApi();
            if (!RateLimiter.allow(rateKey, apiLimit.getMaxCount(), apiLimit.getWindowSeconds())) {
                throw new BusinessException("操作过于频繁，请稍后再试");
            }
            
            return true;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("防重复提交检查异常", e);
            return true; // 异常时放行，避免影响正常业务
        }
    }

    /**
     * 获取用户标识（优先从Token解析，其次从Session）
     */
    private String getUserIdentify(HttpServletRequest request) {
        // 尝试从请求头获取Token
        String token = request.getHeader("Authorization");
        if (token != null && !token.isEmpty()) {
            // 使用Token的hashCode作为用户标识
            return String.valueOf(token.hashCode());
        }
        
        // 尝试从Session获取用户ID
        Object userId = request.getSession().getAttribute("userId");
        if (userId != null) {
            return userId.toString();
        }
        
        // 降级：使用IP地址
        return getClientIp(request);
    }

    /**
     * 获取请求参数（用于生成唯一标识）
     */
    private String getRequestParams(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isEmpty()) {
            return String.valueOf(queryString.hashCode());
        }
        return "empty";
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

package com.education.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 业务配置属性类(从Nacos动态读取)
 * 
 * 功能:
 * 1. 统一管理所有业务配置(JWT/限流/业务常量/邮件)
 * 2. 支持Nacos动态刷新(@RefreshScope)
 * 3. 提供默认值保障(即使Nacos未配置也能正常运行)
 * 
 * 使用示例:
 * <pre>
 * @Autowired
 * private EduBusinessProperties properties;
 * 
 * // 获取JWT配置
 * String secret = properties.getJwt().getSecret();
 * 
 * // 获取限流配置
 * Integer apiMaxCount = properties.getRateLimit().getApi().getMaxCount();
 * 
 * // 获取业务常量
 * BigDecimal questionScore = properties.getBusiness().getDefaultQuestionScore();
 * </pre>
 * 
 * @author system
 * @since 2026-04-09
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "edu")
public class EduBusinessProperties {
    
    /** JWT配置 */
    private JwtProperties jwt = new JwtProperties();
    
    /** 限流配置 */
    private RateLimitProperties rateLimit = new RateLimitProperties();
    
    /** 业务常量 */
    private BusinessProperties business = new BusinessProperties();
    
    /** 邮件配置 */
    private MailProperties mail = new MailProperties();
    
    /**
     * JWT配置属性
     */
    @Data
    public static class JwtProperties {
        /** 密钥(生产环境使用环境变量) */
        private String secret = "edu-training-jwt-secret-key-2026-change-me";
        
        /** Token有效期: 24小时(毫秒) */
        private Long expiration = 86400000L;
        
        /** 刷新Token有效期: 7天(毫秒) */
        private Long refreshExpiration = 604800000L;
    }
    
    /**
     * 限流配置属性
     */
    @Data
    public static class RateLimitProperties {
        /** 短信验证码限流: 5分钟5次 */
        private RateLimitConfig sms = new RateLimitConfig(5, 300);
        
        /** 登录失败限流: 1小时3次 */
        private RateLimitConfig loginFail = new RateLimitConfig(3, 3600);
        
        /** API接口限流: 1分钟60次 */
        private RateLimitConfig api = new RateLimitConfig(60, 60);
        
        /** 文件上传限流: 1小时10次 */
        private RateLimitConfig upload = new RateLimitConfig(10, 3600);
        
        /** 防重复提交限流: 1分钟60次 */
        private RateLimitConfig repeatSubmit = new RateLimitConfig(60, 60);
        
        @Data
        public static class RateLimitConfig {
            /** 限制次数 */
            private Integer maxCount;
            
            /** 窗口时间(秒) */
            private Integer windowSeconds;
            
            public RateLimitConfig() {}
            
            public RateLimitConfig(Integer maxCount, Integer windowSeconds) {
                this.maxCount = maxCount;
                this.windowSeconds = windowSeconds;
            }
        }
    }
    
    /**
     * 业务常量属性
     */
    @Data
    public static class BusinessProperties {
        /** 默认题目分值 */
        private BigDecimal defaultQuestionScore = new BigDecimal("10");
        
        /** 及格分数线 */
        private Integer passScoreThreshold = 60;
        
        /** 默认课程时长(秒) - 60分钟 */
        private Integer defaultCourseDuration = 3600;
        
        /** 进度转换系数(百分比转秒数) */
        private Integer progressConvertFactor = 36;
        
        /** CORS预检缓存时间(秒) */
        private Long corsMaxAge = 3600L;
        
        /** Token即将过期阈值(毫秒) - 30分钟 */
        private Long tokenExpiringSoonThreshold = 1800000L;
        
        /** 验证码有效期(分钟) */
        private Integer captchaExpireMinutes = 5;
    }
    
    /**
     * 邮件配置属性
     */
    @Data
    public static class MailProperties {
        /** 日期格式 */
        private String dateFormat = "yyyy-MM-dd HH:mm:ss";
        
        /** 邮件模板路径 */
        private String templatePath = "classpath:email-templates/";
    }
}

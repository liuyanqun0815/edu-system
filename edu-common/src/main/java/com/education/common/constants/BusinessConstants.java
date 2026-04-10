package com.education.common.constants;

import java.math.BigDecimal;

/**
 * 业务常量(已废弃,请使用EduBusinessProperties)
 * 
 * @deprecated 请使用 {@link com.education.common.config.EduBusinessProperties} 代替
 *             所有常量已迁移到Nacos配置中心,支持动态刷新
 * @author system
 * @since 2026-04-09
 */
@Deprecated
public final class BusinessConstants {
    
    /** 默认题目分值 */
    @Deprecated
    public static final BigDecimal DEFAULT_QUESTION_SCORE = new BigDecimal("10");
    
    /** 及格分数线 */
    @Deprecated
    public static final int PASS_SCORE_THRESHOLD = 60;
    
    /** 默认课程时长(秒) - 60分钟 */
    @Deprecated
    public static final int DEFAULT_COURSE_DURATION = 3600;
    
    /** 进度转换系数(百分比转秒数) */
    @Deprecated
    public static final int PROGRESS_CONVERT_FACTOR = 36;
    
    /** CORS预检缓存时间(秒) */
    @Deprecated
    public static final long CORS_MAX_AGE = 3600L;
    
    /** Token即将过期阈值(毫秒) - 30分钟 */
    @Deprecated
    public static final long TOKEN_EXPIRING_SOON_THRESHOLD = 30 * 60 * 1000;
    
    /** 验证码有效期(分钟) */
    @Deprecated
    public static final int CAPTCHA_EXPIRE_MINUTES = 5;
    
    private BusinessConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

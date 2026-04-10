package com.education.common.constants;

/**
 * 状态常量
 * 
 * @author system
 * @since 2026-04-09
 */
public final class StatusConstants {
    
    /** 禁用/关闭 */
    public static final int DISABLED = 0;
    
    /** 启用/开启 */
    public static final int ENABLED = 1;
    
    /** 学习中 */
    public static final int LEARNING = 1;
    
    /** 已完成 */
    public static final int COMPLETED = 2;
    
    private StatusConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

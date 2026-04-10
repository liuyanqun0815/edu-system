package com.education.common.constants;

/**
 * 角色编码常量
 * 
 * @author system
 * @since 2026-04-09
 */
public final class RoleCodeConstants {
    
    /** 超级管理员 */
    public static final String SUPER_ADMIN = "super_admin";
    
    /** 管理员 */
    public static final String ADMIN = "admin";
    
    /** 教师 */
    public static final String TEACHER = "teacher";
    
    /** 学生 */
    public static final String STUDENT = "student";
    
    private RoleCodeConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

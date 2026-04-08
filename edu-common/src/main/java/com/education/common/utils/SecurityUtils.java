package com.education.common.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 安全工具类
 * 
 * <p>提供密码加密和验证的静态方法，基于BCrypt算法。</p>
 * 
 * <h3>BCrypt算法特点：</h3>
 * <ul>
 *   <li>自动加盐：每次加密结果不同，防止彩虹表攻击</li>
 *   <li>自适应成本因子：可调整计算复杂度以对抗硬件发展</li>
 *   <li>不可逆：加密后无法解密，只能通过matches验证</li>
 * </ul>
 * 
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 密码加密（用户注册/重置密码时）
 * String encryptedPassword = SecurityUtils.encryptPassword("user123");
 * user.setPassword(encryptedPassword);
 * 
 * // 密码验证（用户登录时）
 * boolean isValid = SecurityUtils.matchesPassword(inputPassword, user.getPassword());
 * if (!isValid) {
 *     throw new BusinessException("密码错误");
 * }
 * }</pre>
 * 
 * <h3>注意事项：</h3>
 * <ul>
 *   <li>加密结果长度固定为60字符</li>
 *   <li>数据库密码字段长度建议至少64字符</li>
 *   <li>不要在日志中打印明文密码</li>
 * </ul>
 * 
 * @see org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
 * @author education-team
 */
public class SecurityUtils {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * 密码加密
     */
    public static String encryptPassword(String password) {
        return ENCODER.encode(password);
    }

    /**
     * 密码验证
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }

    /**
     * 生成BCrypt密码
     */
    public static void main(String[] args) {
        System.out.println(encryptPassword("admin123"));
    }
}

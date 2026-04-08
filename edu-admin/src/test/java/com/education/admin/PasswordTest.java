package com.education.admin;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = encoder.encode("admin123");
        System.out.println("加密后的密码: " + password);
        
        // 验证密码
        boolean matches = encoder.matches("admin123", password);
        System.out.println("验证结果: " + matches);
    }
}

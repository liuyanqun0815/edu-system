package com.education.system.service;

/**
 * 邮件Service接口
 */
public interface IEmailService {

    /**
     * 发送验证码邮件
     */
    void sendVerificationCode(String email, String code);

    /**
     * 发送通知邮件
     */
    void sendNotification(String email, String subject, String content);

    /**
     * 批量发送邮件
     */
    void sendBatchEmail(java.util.List<String> emails, String subject, String content);
}

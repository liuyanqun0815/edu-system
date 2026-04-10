package com.education.system.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * 邮件配置类
 * 
 * <p>提供 JavaMailSender bean，即使没有 spring.mail.* 配置也能正常工作。</p>
 * <p>实际的邮件配置由 DynamicMailService 从数据库动态读取。</p>
 */
@Configuration
public class MailConfig {

    /**
     * 创建 JavaMailSender bean
     * 
     * <p>使用默认配置，实际的SMTP配置会在发送时由 DynamicMailService 动态构建。</p>
     */
    @Bean
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        // 设置默认值，实际使用时会被 DynamicMailService 的配置覆盖
        mailSender.setHost("localhost");
        mailSender.setPort(25);
        mailSender.setDefaultEncoding("UTF-8");
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.connectiontimeout", "5000");
        
        return mailSender;
    }
}

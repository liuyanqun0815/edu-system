package com.education.system.service.impl;

import com.education.system.dto.MailConfigDTO;
import com.education.system.service.IDynamicMailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态邮件服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicMailServiceImpl implements IDynamicMailService {

    private volatile JavaMailSenderImpl mailSender;
    private volatile MailConfigDTO currentConfig;
    private final Object lock = new Object();

    @Override
    public void sendMail(String to, String subject, String content) {
        if (mailSender == null) {
            throw new IllegalStateException("邮件配置未初始化");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            
            if (currentConfig.getFromName() != null) {
                helper.setFrom(mailSender.getUsername(), currentConfig.getFromName());
            } else {
                helper.setFrom(mailSender.getUsername());
            }

            mailSender.send(message);
            log.info("邮件发送成功: {}", to);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("邮件发送失败: {}", to, e);
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateMailConfig(MailConfigDTO config) {
        synchronized (lock) {
            JavaMailSenderImpl newSender = new JavaMailSenderImpl();
            newSender.setHost(config.getHost());
            newSender.setPort(config.getPort());
            newSender.setUsername(config.getUsername());
            newSender.setPassword(config.getPassword());

            Properties props = new Properties();
            props.put("mail.smtp.auth", true);
            props.put("mail.smtp.starttls.enable", config.getSslEnabled() != null && config.getSslEnabled());
            props.put("mail.smtp.timeout", config.getTimeout() != null ? config.getTimeout() : 5000);
            props.put("mail.smtp.connectiontimeout", config.getTimeout() != null ? config.getTimeout() : 5000);
            newSender.setJavaMailProperties(props);

            // 测试连接
            try {
                newSender.testConnection();
                this.mailSender = newSender;
                this.currentConfig = config;
                log.info("邮件配置更新成功");
            } catch (Exception e) {
                log.error("邮件配置测试连接失败", e);
                throw new RuntimeException("邮件配置测试连接失败: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public MailConfigDTO getCurrentConfig() {
        return currentConfig;
    }
}

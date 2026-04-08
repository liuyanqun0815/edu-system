package com.education.system.service.mail.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;

/**
 * SMTP邮件发送策略（策略模式）
 * 
 * <p>使用JavaMailSender通过SMTP协议发送邮件。</p>
 */
@Slf4j
@Component
public class SmtpMailStrategy implements MailStrategy {

    private final JavaMailSender mailSender;

    public SmtpMailStrategy(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public boolean send(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // true表示HTML格式
            
            mailSender.send(message);
            log.info("SMTP邮件发送成功: {}", to);
            return true;
        } catch (Exception e) {
            log.error("SMTP邮件发送失败: {}", to, e);
            return false;
        }
    }

    @Override
    public String getStrategyName() {
        return "SMTP";
    }
}

package com.education.system.service;

import com.education.system.dto.SettingGroupVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 动态邮件服务：从 sys_setting 表读取邮件配置，无需在 yml 中硬编码
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicMailService {

    private final ISysSettingService settingService;

    /**
     * 从 sys_setting 表读取 notify 分组的邮件配置，构建 JavaMailSender
     */
    private JavaMailSenderImpl buildMailSender() {
        Map<String, String> notifySettings = getNotifySettings();
        if (notifySettings == null || notifySettings.isEmpty()) {
            log.warn("未找到邮件配置");
            return null;
        }

        String host = notifySettings.get("smtpHost");
        String portStr = notifySettings.get("smtpPort");
        String username = notifySettings.get("smtpFrom");
        String password = notifySettings.get("smtpPassword");

        if (host == null || host.isEmpty() || username == null || username.isEmpty()) {
            log.warn("邮件配置不完整：host={}, username={}", host, username);
            return null;
        }

        int port = 587;
        if (portStr != null && !portStr.isEmpty()) {
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                log.warn("SMTP端口格式错误：{}", portStr);
            }
        }

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password != null ? password : "");
        sender.setDefaultEncoding("UTF-8");

        java.util.Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.connectiontimeout", "5000");

        return sender;
    }

    /**
     * 获取 notify 分组的配置
     */
    private Map<String, String> getNotifySettings() {
        List<SettingGroupVO> allSettings = settingService.listAllGrouped();
        for (SettingGroupVO group : allSettings) {
            if ("notify".equals(group.getGroupCode())) {
                Map<String, String> result = new HashMap<>();
                for (SettingGroupVO.SettingItemVO item : group.getItems()) {
                    result.put(item.getSettingKey(), item.getSettingValue());
                }
                return result;
            }
        }
        return null;
    }

    /**
     * 判断邮件功能是否已启用
     */
    public boolean isEnabled() {
        try {
            Map<String, String> notifySettings = getNotifySettings();
            if (notifySettings == null) {
                return false;
            }
            String enabled = notifySettings.get("emailEnabled");
            String password = notifySettings.get("smtpPassword");
            return "true".equalsIgnoreCase(enabled) && password != null && !password.isEmpty();
        } catch (Exception e) {
            log.warn("检查邮件配置失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取发件人邮箱
     */
    public String getFromEmail() {
        Map<String, String> notifySettings = getNotifySettings();
        return notifySettings != null ? notifySettings.getOrDefault("smtpFrom", "") : "";
    }

    /**
     * 发送 HTML 邮件
     *
     * @param to      收件人
     * @param subject 主题
     * @param body    HTML 内容
     */
    public void send(String to, String subject, String body) {
        if (!isEnabled()) {
            log.info("邮件功能未启用或未配置授权码，跳过发送 to={}", to);
            return;
        }
        try {
            JavaMailSenderImpl sender = buildMailSender();
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(getFromEmail());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            sender.send(message);
            log.info("邮件发送成功 to={}", to);
        } catch (Exception e) {
            log.warn("邮件发送失败 to={}: {}", to, e.getMessage());
        }
    }
}

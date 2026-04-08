package com.education.system.service;

import com.education.common.result.JsonResult;
import com.education.system.dto.SettingGroupVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 邮件发送服务类
 * 
 * <p>提供邮件发送功能，支持简单文本邮件和HTML格式邮件。</p>
 * 
 * <h3>动态配置机制：</h3>
 * <p>邮件服务器配置从 sys_setting 表动态获取，而非application.yml静态配置，
 * 支持运行时修改邮件配置无需重启服务。</p>
 * 
 * <h3>配置项（sys_setting表）：</h3>
 * <table border="1">
 *   <tr><th>配置键</th><th>说明</th></tr>
 *   <tr><td>notify:smtpHost</td><td>SMTP服务器地址</td></tr>
 *   <tr><td>notify:smtpPort</td><td>SMTP端口（默认587）</td></tr>
 *   <tr><td>notify:smtpFrom</td><td>发件人邮箱（SMTP用户名）</td></tr>
 *   <tr><td>notify:smtpPassword</td><td>SMTP密码/授权码</td></tr>
 * </table>
 * 
 * <h3>使用示例：</h3>
 * <pre>{@code
 * @Autowired
 * private EmailService emailService;
 * 
 * // 发送简单邮件
 * emailService.sendSimpleMail("user@example.com", "主题", "内容");
 * 
 * // 发送HTML邮件
 * String html = "<h1>欢迎</h1><p>这是一封HTML邮件</p>";
 * emailService.sendHtmlMail("user@example.com", "主题", html);
 * }</pre>
 * 
 * <h3>应用场景：</h3>
 * <ul>
 *   <li>用户注册验证码</li>
 *   <li>密码重置链接</li>
 *   <li>课程提醒通知</li>
 *   <li>考试结果通知</li>
 * </ul>
 * 
 * @see ISysSettingService 系统设置服务
 * @see DynamicMailService 动态邮件服务
 * @author education-team
 */
@Slf4j
@Service
public class EmailService {

    @Autowired
    private ISysSettingService settingService;

    // 动态获取的邮件发送器
    private JavaMailSender dynamicMailSender;
    private String fromEmail;

    /**
     * 初始化邮件配置（从sys_setting动态获取）
     */
    private void initMailConfig() {
        List<SettingGroupVO> allSettings = settingService.listAllGrouped();
        Map<String, String> notifySettings = null;
        
        // 查找 notify 分组
        for (SettingGroupVO group : allSettings) {
            if ("notify".equals(group.getGroupCode())) {
                notifySettings = new HashMap<>();
                for (SettingGroupVO.SettingItemVO item : group.getItems()) {
                    notifySettings.put(item.getSettingKey(), item.getSettingValue());
                }
                break;
            }
        }
        
        if (notifySettings == null) {
            log.warn("未找到邮件配置");
            return;
        }

        String host = notifySettings.get("smtpHost");
        String portStr = notifySettings.get("smtpPort");
        String username = notifySettings.get("smtpFrom");  // 发件人邮箱即SMTP用户名
        String password = notifySettings.get("smtpPassword");

        if (host == null || username == null || password == null) {
            log.warn("邮件配置不完整：host={}, username={}", host, username);
            return;
        }

        int port = 587;
        if (portStr != null) {
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                log.warn("SMTP端口格式错误：{}", portStr);
            }
        }

        // 创建动态邮件发送器
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false");

        this.dynamicMailSender = sender;
        this.fromEmail = username;
        log.info("邮件配置初始化成功：host={}, port={}, user={}", host, port, username);
    }

    /**
     * 发送简单邮件
     */
    public JsonResult sendSimpleMail(String to, String subject, String content) {
        try {
            // 确保配置已初始化
            if (dynamicMailSender == null) {
                initMailConfig();
            }
            if (dynamicMailSender == null) {
                return JsonResult.error("邮件服务未配置，请先在系统设置中配置SMTP信息");
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            dynamicMailSender.send(message);
            log.info("邮件发送成功：to={}, subject={}", to, subject);
            return JsonResult.success("邮件发送成功");
        } catch (Exception e) {
            log.error("邮件发送失败：", e);
            return JsonResult.error("邮件发送失败：" + e.getMessage());
        }
    }

    /**
     * 发送HTML邮件
     */
    public JsonResult sendHtmlMail(String to, String subject, String htmlContent) {
        try {
            // 确保配置已初始化
            if (dynamicMailSender == null) {
                initMailConfig();
            }
            if (dynamicMailSender == null) {
                return JsonResult.error("邮件服务未配置，请先在系统设置中配置SMTP信息");
            }

            MimeMessage message = dynamicMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            dynamicMailSender.send(message);
            log.info("HTML邮件发送成功：to={}, subject={}", to, subject);
            return JsonResult.success("邮件发送成功");
        } catch (Exception e) {
            log.error("HTML邮件发送失败：", e);
            return JsonResult.error("邮件发送失败：" + e.getMessage());
        }
    }

    /**
     * 重新加载邮件配置（保存配置后调用）
     */
    public void reloadMailConfig() {
        dynamicMailSender = null;
        fromEmail = null;
        initMailConfig();
    }
}

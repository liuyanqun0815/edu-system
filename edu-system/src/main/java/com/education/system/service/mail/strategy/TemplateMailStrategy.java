package com.education.system.service.mail.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模板邮件发送策略（策略模式）
 * 
 * <p>基于预定义模板发送邮件，支持变量替换。</p>
 * 
 * <h3>使用示例：</h3>
 * <pre>
 * TemplateMailStrategy strategy = new TemplateMailStrategy();
 * strategy.registerTemplate("welcome", "欢迎{name}加入系统");
 * 
 * Map<String, Object> params = new HashMap<>();
 * params.put("name", "张三");
 * strategy.sendWithTemplate("user@example.com", "欢迎", "welcome", params);
 * </pre>
 */
@Slf4j
@Component
public class TemplateMailStrategy implements MailStrategy {

    /**
     * 模板存储
     */
    private final Map<String, String> templates = new ConcurrentHashMap<>();

    public TemplateMailStrategy() {
        // 注册默认模板
        registerDefaultTemplates();
    }

    /**
     * 注册默认模板
     */
    private void registerDefaultTemplates() {
        templates.put("verify_code", 
            "<div style='padding:20px;background:#f5f5f5;'>" +
            "<h2 style='color:#333;'>验证码通知</h2>" +
            "<p>您的验证码是：<strong style='color:#e74c3c;font-size:24px;'>{code}</strong></p>" +
            "<p style='color:#999;font-size:12px;'>验证码5分钟内有效，请及时使用。</p>" +
            "</div>"
        );
        
        templates.put("password_reset",
            "<div style='padding:20px;background:#f5f5f5;'>" +
            "<h2 style='color:#333;'>密码重置</h2>" +
            "<p>您的重置验证码是：<strong style='color:#e74c3c;font-size:24px;'>{code}</strong></p>" +
            "<p style='color:#999;font-size:12px;'>如非本人操作，请忽略此邮件。</p>" +
            "</div>"
        );
        
        templates.put("lesson_notify",
            "<div style='padding:20px;background:#f5f5f5;'>" +
            "<h2 style='color:#333;'>课程提醒</h2>" +
            "<p>亲爱的{name}：</p>" +
            "<p>您有一节课程即将开始：</p>" +
            "<ul>" +
            "<li>课程名称：{lessonName}</li>" +
            "<li>开始时间：{startTime}</li>" +
            "</ul>" +
            "<p style='color:#999;font-size:12px;'>请准时参加。</p>" +
            "</div>"
        );
    }

    /**
     * 注册模板
     * 
     * @param templateName 模板名称
     * @param content 模板内容
     */
    public void registerTemplate(String templateName, String content) {
        templates.put(templateName, content);
    }

    /**
     * 使用模板发送邮件
     * 
     * @param to 收件人
     * @param subject 主题
     * @param templateName 模板名称
     * @param params 模板参数
     * @return 是否成功
     */
    public boolean sendWithTemplate(String to, String subject, String templateName, Map<String, Object> params) {
        String template = templates.get(templateName);
        if (template == null) {
            log.error("邮件模板不存在: {}", templateName);
            return false;
        }

        // 替换模板变量
        String content = template;
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                content = content.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
            }
        }

        return send(to, subject, content);
    }

    @Override
    public boolean send(String to, String subject, String content) {
        log.info("模板邮件发送: {} - {}", to, subject);
        // 实际发送需依赖JavaMailSender，这里仅记录日志
        // 可通过构造函数注入JavaMailSender或使用SmtpMailStrategy
        return true;
    }

    @Override
    public String getStrategyName() {
        return "TEMPLATE";
    }
}

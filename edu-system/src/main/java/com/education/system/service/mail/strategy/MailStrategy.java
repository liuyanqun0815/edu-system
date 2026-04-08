package com.education.system.service.mail.strategy;

/**
 * 邮件发送策略接口（策略模式）
 * 
 * <p>定义邮件发送的统一接口，支持多种实现方式。</p>
 * 
 * <h3>实现类：</h3>
 * <ul>
 *   <li>SmtpMailStrategy - SMTP协议发送</li>
 *   <li>TemplateMailStrategy - 模板邮件发送</li>
 *   <li>AsyncMailStrategy - 异步邮件发送（基于Redis消息队列）</li>
 * </ul>
 * 
 * <h3>使用示例：</h3>
 * <pre>
 * MailStrategy strategy = new SmtpMailStrategy();
 * strategy.send("user@example.com", "标题", "内容");
 * </pre>
 */
public interface MailStrategy {

    /**
     * 发送邮件
     * 
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     * @return 是否发送成功
     */
    boolean send(String to, String subject, String content);

    /**
     * 获取策略名称
     * 
     * @return 策略名称
     */
    String getStrategyName();
}

package com.education.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.system.entity.SysConfig;
import com.education.system.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 动态邮件服务：从 sys_config 表读取邮件配置，无需在 yml 中硬编码
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicMailService {

    private final SysConfigMapper configMapper;

    /**
     * 从数据库读取 email.* 配置，构建 JavaMailSender
     */
    private JavaMailSenderImpl buildMailSender() {
        List<SysConfig> configs = configMapper.selectList(
                new LambdaQueryWrapper<SysConfig>()
                        .likeRight(SysConfig::getConfigKey, "email.")
        );
        Map<String, String> cfgMap = configs.stream()
                .collect(Collectors.toMap(SysConfig::getConfigKey, c -> c.getConfigValue() != null ? c.getConfigValue() : ""));

        String host = cfgMap.getOrDefault("email.smtp.host", "smtp.qq.com");
        String portStr = cfgMap.getOrDefault("email.smtp.port", "587");
        String username = cfgMap.getOrDefault("email.smtp.username", "");
        String password = cfgMap.getOrDefault("email.smtp.password", "");
        boolean ssl = "true".equalsIgnoreCase(cfgMap.getOrDefault("email.smtp.ssl", "false"));

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            port = ssl ? 465 : 587;
        }

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);
        sender.setDefaultEncoding("UTF-8");

        java.util.Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        if (ssl) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        } else {
            props.put("mail.smtp.starttls.enable", "true");
        }
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.connectiontimeout", "5000");

        return sender;
    }

    /**
     * 判断邮件功能是否已启用（sys_config 中 email.enabled = 1 且 password 不为空）
     */
    public boolean isEnabled() {
        try {
            SysConfig enabledCfg = configMapper.selectOne(
                    new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, "email.enabled"));
            if (enabledCfg == null || !"1".equals(enabledCfg.getConfigValue())) {
                return false;
            }
            SysConfig pwdCfg = configMapper.selectOne(
                    new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, "email.smtp.password"));
            return pwdCfg != null && pwdCfg.getConfigValue() != null && !pwdCfg.getConfigValue().isEmpty();
        } catch (Exception e) {
            log.warn("检查邮件配置失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取发件人邮箱（读自 sys_config: email.from）
     */
    public String getFromEmail() {
        SysConfig cfg = configMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, "email.from"));
        return cfg != null && cfg.getConfigValue() != null ? cfg.getConfigValue() : "";
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

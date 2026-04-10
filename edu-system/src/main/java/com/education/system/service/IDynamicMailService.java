package com.education.system.service;

import com.education.system.dto.MailConfigDTO;

/**
 * 动态邮件Service接口
 */
public interface IDynamicMailService {

    /**
     * 发送邮件
     */
    void sendMail(String to, String subject, String content);

    /**
     * 更新邮件配置
     */
    void updateMailConfig(MailConfigDTO config);

    /**
     * 获取当前邮件配置
     */
    MailConfigDTO getCurrentConfig();
}

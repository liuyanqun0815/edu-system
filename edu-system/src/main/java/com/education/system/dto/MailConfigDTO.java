package com.education.system.dto;

import lombok.Data;

/**
 * 邮件配置DTO
 */
@Data
public class MailConfigDTO {

    /**
     * SMTP服务器地址
     */
    private String host;

    /**
     * SMTP端口
     */
    private Integer port;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 是否启用SSL
     */
    private Boolean sslEnabled;

    /**
     * 发件人昵称
     */
    private String fromName;

    /**
     * 超时时间(毫秒)
     */
    private Integer timeout;
}

package com.education.system.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 登录DTO
 */
@Data
public class LoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    /** 验证码key（前端从/auth/captcha获取） */
    private String captchaKey;

    /** 用户输入的验证码 */
    private String captchaCode;
}

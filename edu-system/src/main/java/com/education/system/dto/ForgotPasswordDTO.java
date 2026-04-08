package com.education.system.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 找回密码DTO
 */
@Data
public class ForgotPasswordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户名或邮箱 */
    @NotBlank(message = "用户名或邮箱不能为空")
    private String usernameOrEmail;
}

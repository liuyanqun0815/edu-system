package com.education.system.dto;

import lombok.Data;

/**
 * 验证码 VO
 */
@Data
public class CaptchaVO {
    
    /**
     * 验证码Key
     */
    private String captchaKey;
    
    /**
     * 验证码图片（Base64）
     */
    private String captchaImg;
}

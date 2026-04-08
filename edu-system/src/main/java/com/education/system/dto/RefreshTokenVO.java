package com.education.system.dto;

import lombok.Data;

/**
 * Token 刷新 VO
 */
@Data
public class RefreshTokenVO {
    
    /**
     * 访问令牌
     */
    private String token;
    
    /**
     * 刷新令牌
     */
    private String refreshToken;
}

package com.education.system.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 登录返回VO
 */
@Data
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * Token
     */
    private String token;

    /**
     * 刷新Token
     */
    private String refreshToken;

    /**
     * 菜单列表
     */
    private List<?> menus;
}

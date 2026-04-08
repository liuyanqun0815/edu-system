package com.education.system.chain;

import com.education.system.entity.SysUser;
import lombok.Data;

/**
 * 登录上下文（责任链模式数据载体）
 */
@Data
public class LoginContext {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 验证码
     */
    private String captcha;

    /**
     * 验证码Key
     */
    private String captchaKey;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 查询到的用户对象
     */
    private SysUser user;

    /**
     * 校验是否成功
     */
    private boolean success = true;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 标记校验失败
     * 
     * @param errorMsg 错误信息
     */
    public void fail(String errorMsg) {
        this.success = false;
        this.errorMsg = errorMsg;
    }
}

package com.education.system.chain;

/**
 * 登录校验链处理器（责任链模式）
 * 
 * <p>定义校验链节点的抽象基类，支持动态组合校验规则。</p>
 * 
 * <h3>校验流程：</h3>
 * <pre>
 * 验证码校验 → 用户存在性 → 密码校验 → 状态校验 → IP限制校验
 * </pre>
 * 
 * <h3>扩展示例：</h3>
 * <pre>
 * // 创建校验节点
 * CaptchaValidator captchaValidator = new CaptchaValidator();
 * PasswordValidator passwordValidator = new PasswordValidator();
 * 
 * // 组装责任链
 * captchaValidator.setNext(passwordValidator);
 * 
 * // 执行校验
 * captchaValidator.validate(loginDTO);
 * </pre>
 */
public abstract class LoginValidator {

    /**
     * 下一个校验节点
     */
    protected LoginValidator nextValidator;

    /**
     * 设置下一个校验节点
     * 
     * @param nextValidator 下一个校验器
     * @return 当前校验器（支持链式调用）
     */
    public LoginValidator setNext(LoginValidator nextValidator) {
        this.nextValidator = nextValidator;
        return this;
    }

    /**
     * 执行校验
     * 
     * @param context 登录上下文
     */
    public final void validate(LoginContext context) {
        // 执行当前校验
        doValidate(context);
        
        // 如果校验通过且有下一个节点，继续校验
        if (context.isSuccess() && nextValidator != null) {
            nextValidator.validate(context);
        }
    }

    /**
     * 具体校验逻辑（子类实现）
     * 
     * @param context 登录上下文
     */
    protected abstract void doValidate(LoginContext context);
}

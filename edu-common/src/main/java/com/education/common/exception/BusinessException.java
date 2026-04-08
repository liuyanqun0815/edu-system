package com.education.common.exception;

import lombok.Getter;

/**
 * 业务异常类
 * 
 * <p>用于在业务逻辑中抛出可预期的异常，由 {@link GlobalExceptionHandler} 统一捕获处理。</p>
 * 
 * <h3>设计理念：</h3>
 * <ul>
 *   <li>业务异常：用户操作不当或业务规则校验失败（如用户名已存在、余额不足）</li>
 *   <li>系统异常：程序bug或外部服务故障（由Exception派生，不使用此类）</li>
 * </ul>
 * 
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 简单用法：默认错误码500
 * if (user == null) {
 *     throw new BusinessException("用户不存在");
 * }
 * 
 * // 指定错误码
 * if (balance < amount) {
 *     throw new BusinessException(400, "余额不足，当前余额：" + balance);
 * }
 * 
 * // 包装底层异常
 * try {
 *     // 调用外部服务
 * } catch (RemoteException e) {
 *     throw new BusinessException(503, "第三方服务不可用", e);
 * }
 * }</pre>
 * 
 * <h3>异常处理流程：</h3>
 * <pre>
 * Service层抛出 → GlobalExceptionHandler捕获 → 转换为JsonResult返回前端
 * </pre>
 * 
 * @see GlobalExceptionHandler#handleBusinessException(BusinessException, javax.servlet.http.HttpServletRequest)
 * @author education-team
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误消息
     */
    private String msg;

    public BusinessException(String msg) {
        super(msg);
        this.code = 500;
        this.msg = msg;
    }

    public BusinessException(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public BusinessException(Integer code, String msg, Throwable cause) {
        super(msg, cause);
        this.code = code;
        this.msg = msg;
    }
}

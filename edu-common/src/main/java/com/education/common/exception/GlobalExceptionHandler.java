package com.education.common.exception;

import com.education.common.result.JsonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器
 * 
 * <p>使用 @RestControllerAdvice 统一捕获处理Controller层抛出的各类异常，
 * 将异常信息转换为标准的 JsonResult 格式返回给前端。</p>
 * 
 * <h3>处理的异常类型：</h3>
 * <table border="1">
 *   <tr><th>异常类型</th><th>处理方式</th><th>返回状态码</th></tr>
 *   <tr><td>BusinessException</td><td>业务异常，返回友好提示</td><td>自定义code</td></tr>
 *   <tr><td>BindException</td><td>参数校验失败</td><td>400</td></tr>
 *   <tr><td>SQLIntegrityConstraintViolationException</td><td>数据库唯一约束冲突</td><td>400</td></tr>
 *   <tr><td>Exception</td><td>其他未知异常</td><td>500</td></tr>
 * </table>
 * 
 * <h3>日志记录策略：</h3>
 * <ul>
 *   <li>业务异常：记录error级别，包含请求URI</li>
 *   <li>参数异常：记录error级别</li>
 *   <li>数据库约束：记录warn级别（非程序错误）</li>
 *   <li>系统异常：记录error级别，包含完整堆栈</li>
 * </ul>
 * 
 * <h3>注意事项：</h3>
 * <ul>
 *   <li>不要在业务代码中catch异常后返回JsonResult，应直接抛出BusinessException</li>
 *   <li>敏感异常信息不应直接暴露给前端</li>
 * </ul>
 * 
 * @see BusinessException 业务异常类
 * @see JsonResult 统一响应结果
 * @author education-team
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public JsonResult<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("业务异常：{}，请求地址：{}", e.getMsg(), request.getRequestURI());
        return JsonResult.error(e.getCode(), e.getMsg());
    }

    /**
     * 参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public JsonResult<Void> handleBindException(BindException e, HttpServletRequest request) {
        String message = e.getAllErrors().get(0).getDefaultMessage();
        log.error("参数绑定异常：{}，请求地址：{}", message, request.getRequestURI());
        return JsonResult.error(400, message);
    }

    /**
     * 数据库唯一约束冲突异常（如邮箱、手机号、用户名重复）
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public JsonResult<Void> handleSQLIntegrityConstraintViolationException(
            SQLIntegrityConstraintViolationException e, HttpServletRequest request) {
        log.warn("唯一约束冲突：{}，请求地址：{}", e.getMessage(), request.getRequestURI());
        String msg = e.getMessage();
        if (msg != null && msg.contains("Duplicate entry")) {
            if (msg.contains("uk_user_email") || msg.contains("email")) {
                return JsonResult.error(400, "该邮箱已被其他用户使用，请更换邮箱");
            }
            if (msg.contains("uk_user_phone") || msg.contains("phone")) {
                return JsonResult.error(400, "该手机号已被其他用户使用，请更换手机号");
            }
            if (msg.contains("uk_username") || msg.contains("username")) {
                return JsonResult.error(400, "该用户名已存在，请更换用户名");
            }
            // 通用唯一键冲突提示（提取重复值）
            try {
                String entry = msg.replaceAll(".*Duplicate entry '([^']+)'.*", "$1");
                return JsonResult.error(400, "数据重复：\"" + entry + "\" 已存在，请修改后重试");
            } catch (Exception ex) {
                return JsonResult.error(400, "提交的数据与已有记录冲突，请检查后重试");
            }
        }
        return JsonResult.error(400, "数据约束冲突，请检查填写内容后重试");
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public JsonResult<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常：{}，请求地址：{}", e.getMessage(), request.getRequestURI(), e);
        return JsonResult.error("系统繁忙，请稍后重试");
    }
}

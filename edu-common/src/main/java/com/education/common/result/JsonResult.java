package com.education.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一API响应结果封装类
 * 
 * <p>所有Controller接口的统一返回格式，前后端交互的标准数据结构。</p>
 * 
 * <h3>响应结构：</h3>
 * <pre>
 * {
 *   "code": 200,          // 状态码：200成功，其他失败
 *   "msg": "操作成功",    // 提示信息
 *   "data": { ... }       // 业务数据（泛型T）
 * }
 * </pre>
 * 
 * <h3>常用状态码约定：</h3>
 * <ul>
 *   <li>200 - 操作成功</li>
 *   <li>400 - 参数错误/请求格式不正确</li>
 *   <li>500 - 服务器内部错误</li>
 * </ul>
 * 
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 成功返回（无数据）
 * return JsonResult.success();
 * 
 * // 成功返回（带数据）
 * return JsonResult.success(user);
 * 
 * // 成功返回（自定义消息）
 * return JsonResult.success("查询成功", userList);
 * 
 * // 失败返回
 * return JsonResult.error("用户名已存在");
 * return JsonResult.error(400, "参数不能为空");
 * }</pre>
 * 
 * @param <T> 业务数据类型
 * @see PageResult 分页结果封装
 * @see com.education.common.exception.GlobalExceptionHandler 全局异常处理
 * @author education-team
 */
@Data
public class JsonResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码 200-成功 其他-失败
     */
    private Integer code;

    /**
     * 返回消息
     */
    private String msg;

    /**
     * 返回数据
     */
    private T data;

    public JsonResult() {
    }

    public JsonResult(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 成功返回结果
     */
    public static <T> JsonResult<T> success() {
        return new JsonResult<>(200, "操作成功", null);
    }

    /**
     * 成功返回结果
     */
    public static <T> JsonResult<T> success(T data) {
        return new JsonResult<>(200, "操作成功", data);
    }

    /**
     * 成功返回结果（带消息）
     */
    public static <T> JsonResult<T> success(String msg) {
        return new JsonResult<>(200, msg, null);
    }

    /**
     * 成功返回结果
     */
    public static <T> JsonResult<T> success(String msg, T data) {
        return new JsonResult<>(200, msg, data);
    }

    /**
     * 失败返回结果
     */
    public static <T> JsonResult<T> error() {
        return new JsonResult<>(500, "操作失败", null);
    }

    /**
     * 失败返回结果
     */
    public static <T> JsonResult<T> error(String msg) {
        return new JsonResult<>(500, msg, null);
    }

    /**
     * 失败返回结果
     */
    public static <T> JsonResult<T> error(Integer code, String msg) {
        return new JsonResult<>(code, msg, null);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return code != null && code == 200;
    }
}

package com.education.common.result;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * RPC统一返回结果包装类
 * 用于Service层返回，包含状态码、消息、业务数据和附加信息
 * 
 * @param <T> 业务数据类型
 */
public class RpcResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "状态码", required = true)
    private int code;

    @ApiModelProperty(value = "消息内容", required = true)
    private String msg;

    @ApiModelProperty(value = "时间戳", required = true)
    private long serverTime;

    @ApiModelProperty(value = "错误堆栈", required = false)
    private Throwable cause;

    @ApiModelProperty(value = "业务数据", required = false)
    private T data;

    @ApiModelProperty(value = "附加业务数据", required = false)
    private Map<String, Object> attachments;

    public RpcResult() {
        this.code = 200;
        this.msg = "成功";
        this.serverTime = System.currentTimeMillis();
        this.attachments = new HashMap<>();
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return this.code == 200;
    }

    /**
     * 成功返回（带数据）
     */
    public static <T> RpcResult<T> success(T data) {
        RpcResult<T> result = new RpcResult<>();
        result.setData(data);
        return result;
    }

    /**
     * 成功返回（带附加信息）
     */
    public static <T> RpcResult<T> successAddAttachment(T data, String key, String value) {
        RpcResult<T> result = new RpcResult<>();
        result.setData(data);
        result.addAttachment(key, value);
        return result;
    }

    /**
     * 成功返回（仅附加信息）
     */
    public static <T> RpcResult<T> successAddAttachment(String key, String value) {
        return successAddAttachment(null, key, value);
    }

    /**
     * 失败返回
     */
    public static <T> RpcResult<T> fail(String msg) {
        return fail(msg, null);
    }

    /**
     * 失败返回（带错误码）
     */
    public static <T> RpcResult<T> fail(int code, String msg) {
        return fail(code, msg, null);
    }

    /**
     * 失败返回（带异常）
     */
    public static <T> RpcResult<T> fail(String msg, Throwable cause) {
        return fail(500, msg, cause);
    }

    /**
     * 失败返回（完整参数）
     */
    public static <T> RpcResult<T> fail(int code, String msg, Throwable cause) {
        RpcResult<T> result = new RpcResult<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setCause(cause);
        return result;
    }

    /**
     * 添加附加信息
     */
    public void addAttachment(String key, Object value) {
        if (this.attachments == null) {
            this.attachments = new HashMap<>();
        }
        this.attachments.put(key, value);
    }

    // Getter and Setter
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getServerTime() {
        return serverTime;
    }

    public void setServerTime(long serverTime) {
        this.serverTime = serverTime;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Map<String, Object> getAttachments() {
        return attachments;
    }

    public void setAttachments(Map<String, Object> attachments) {
        this.attachments = attachments;
    }
}

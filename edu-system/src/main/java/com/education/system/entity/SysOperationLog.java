package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 操作日志实体
 */
@Data
@TableName("sys_operation_log")
public class SysOperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作类型:增/删/改/查/导入/导出
     */
    private String operationType;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求方式:GET/POST
     */
    private String requestMethod;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 响应结果
     */
    private String responseResult;

    /**
     * 操作IP
     */
    private String ip;

    /**
     * 操作地点
     */
    private String location;

    /**
     * 执行时间(毫秒)
     */
    private Long costTime;

    /**
     * 状态:0-失败 1-成功
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 操作时间
     */
    private Date operationTime;
}

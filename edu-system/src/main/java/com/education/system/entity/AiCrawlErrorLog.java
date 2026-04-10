package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 抓取异常日志表实体
 */
@Data
@TableName("ai_crawl_error_log")
public class AiCrawlErrorLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 日志ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联任务ID */
    private Long taskId;

    /** 错误类型: timeout-超时, network-网络异常, parse-解析失败, rate_limit-频率限制, other-其他 */
    private String errorType;

    /** 错误级别:1-警告 2-错误 3-严重 */
    private Integer errorLevel;

    /** 错误信息 */
    private String errorMsg;

    /** 堆栈信息 */
    private String stackTrace;

    /** 请求URL */
    private String requestUrl;

    /** HTTP响应码 */
    private Integer responseCode;

    /** 已重试次数 */
    private Integer retryCount;

    /** 创建时间 */
    private Date createTime;
}

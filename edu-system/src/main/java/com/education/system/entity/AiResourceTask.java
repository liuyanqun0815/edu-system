package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * AI资源抓取任务实体
 */
@Data
@TableName("ai_resource_task")
public class AiResourceTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 任务ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 任务名称 */
    private String taskName;

    /** 任务类型: exam_paper-试卷, article-文章, journal-外刊, news-新闻 */
    private String taskType;

    /** 学科(英语/数学/语文等) */
    private String subject;

    /** 年级 */
    private String grade;

    /** 资源来源URL */
    private String sourceUrl;

    /** 来源网站名称 */
    private String sourceName;

    /** 抓取状态:0-待执行 1-执行中 2-成功 3-失败 4-超时 */
    private Integer crawlStatus;

    /** 解析状态:0-未解析 1-解析中 2-成功 3-失败 */
    private Integer parseStatus;

    /** 导入状态:0-未导入 1-导入中 2-成功 3-失败 */
    private Integer importStatus;

    /** 重试次数 */
    private Integer retryCount;

    /** 最大重试次数 */
    private Integer maxRetry;

    /** 超时时间(秒),默认5分钟 */
    private Integer timeoutSeconds;

    /** 错误信息 */
    private String errorMsg;

    /** 开始时间 */
    private Date startTime;

    /** 结束时间 */
    private Date endTime;

    /** 下次重试时间 */
    private Date nextRetryTime;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 逻辑删除:0-正常 1-删除 */
    private Integer dr;
}

package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * AI资源表实体
 */
@Data
@TableName("ai_resource")
public class AiResource implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 资源ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联任务ID */
    private Long taskId;

    /** 资源类型: exam_paper-试卷, article-文章, journal-外刊 */
    private String resourceType;

    /** 学科 */
    private String subject;

    /** 年级 */
    private String grade;

    /** 资源标题 */
    private String title;

    /** 资源摘要 */
    private String summary;

    /** 资源内容(JSON格式存储结构化数据) */
    private String content;

    /** 原始内容(HTML/文本) */
    private String rawContent;

    /** 文件URL(如PDF) */
    private String fileUrl;

    /** 图片URL列表(JSON数组) */
    private String imageUrls;

    /** 难度:1-简单 2-中等 3-困难 */
    private Integer difficulty;

    /** 考试年份(如为试卷) */
    private Integer examYear;

    /** 考试地区(如为试卷) */
    private String examRegion;

    /** 字数统计 */
    private Integer wordCount;

    /** 质量评分(0-5) */
    private BigDecimal qualityScore;

    /** 标签(逗号分隔) */
    private String tags;

    /** 来源URL */
    private String sourceUrl;

    /** 状态:0-待审核 1-已通过 2-已拒绝 3-已入库 */
    private Integer status;

    /** 审核意见 */
    private String reviewComment;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 逻辑删除:0-正常 1-删除 */
    private Integer dr;
}

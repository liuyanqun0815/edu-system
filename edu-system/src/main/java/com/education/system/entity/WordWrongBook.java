package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 单词错题本实体
 */
@Data
@TableName("word_wrong_book")
public class WordWrongBook implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 单词ID
     */
    private Long wordId;

    /**
     * 题目类型
     */
    private String questionType;

    /**
     * 错误次数
     */
    private Integer wrongCount;

    /**
     * 最后错误时间
     */
    private Date lastWrongTime;

    /**
     * 复习状态:0-待复习 1-已掌握 2-需强化
     */
    private Integer reviewStatus;

    /**
     * 下次复习日期(艾宾浩斯遗忘曲线)
     */
    private Date nextReviewDate;

    /**
     * 掌握程度:0-未掌握 1-部分掌握 2-已掌握
     */
    private Integer masteryLevel;

    /**
     * 用户备注
     */
    private String note;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 逻辑删除:0-正常 1-删除
     */
    private Integer dr;
}


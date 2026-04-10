package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 单词训练会话实体
 */
@Data
@TableName("word_training_session")
public class WordTrainingSession implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 年级(如:六年级、初一、高三)
     */
    private String grade;

    /**
     * 难度:1-简单 2-中等 3-困难
     */
    private Integer difficulty;

    /**
     * 题目类型:spell-拼写,choice_select-看词选义,choice_word-看义选词,translate-翻译,sentence_fill-例句填空
     */
    private String questionType;

    /**
     * 题目数量
     */
    private Integer questionCount;

    /**
     * 每题时间(秒),默认30秒
     */
    private Integer perQuestionTime;

    /**
     * 总分
     */
    private Integer totalScore;

    /**
     * 正确题数
     */
    private Integer correctCount;

    /**
     * 错误题数
     */
    private Integer wrongCount;

    /**
     * 跳答题数
     */
    private Integer skipCount;

    /**
     * 状态:0-进行中 1-已完成 2-已放弃
     */
    private Integer status;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 实际用时(秒)
     */
    private Integer actualDuration;

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


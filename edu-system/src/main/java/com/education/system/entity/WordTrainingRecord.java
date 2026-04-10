package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 单词训练答题记录实体
 */
@Data
@TableName("word_training_record")
public class WordTrainingRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID
     */
    private Long sessionId;

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
     * 题目内容(如:例句填空的题干)
     */
    private String questionContent;

    /**
     * 正确答案
     */
    private String correctAnswer;

    /**
     * 用户答案
     */
    private String userAnswer;

    /**
     * 是否正确:0-错误 1-正确 NULL-未作答
     */
    private Integer isCorrect;

    /**
     * 是否跳过:0-否 1-是
     */
    private Integer isSkipped;

    /**
     * 答题用时(秒)
     */
    private Integer answerTime;

    /**
     * 是否超时:0-否 1-是
     */
    private Integer timeout;

    /**
     * 题目顺序
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}

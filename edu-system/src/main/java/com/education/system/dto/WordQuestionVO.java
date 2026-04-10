package com.education.system.dto;

import lombok.Data;

/**
 * 单词题目VO
 */
@Data
public class WordQuestionVO {

    /**
     * 题目序号
     */
    private Integer questionIndex;

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
     * 单词
     */
    private String word;

    /**
     * 音标
     */
    private String phonetic;

    /**
     * 中文释义
     */
    private String translation;

    /**
     * 例句
     */
    private String example;

    /**
     * 选项列表(用于选择题,JSON格式:[{"label":"A","content":"..."}])
     */
    private String options;

    /**
     * 正确答案(前端不返回,仅用于后端校验)
     */
    private transient String correctAnswer;

    /**
     * 每题时间(秒)
     */
    private Integer timeLimit;
}

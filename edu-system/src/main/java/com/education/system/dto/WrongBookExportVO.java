package com.education.system.dto;

import lombok.Data;

import java.util.Date;

/**
 * 错题导出VO
 */
@Data
public class WrongBookExportVO {

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
     * 掌握程度
     */
    private Integer masteryLevel;

    /**
     * 例句
     */
    private String example;
}

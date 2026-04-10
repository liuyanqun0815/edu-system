package com.education.system.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 解析结果DTO
 */
@Data
public class ParseResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 试卷标题 */
    private String title;

    /** 试卷内容(HTML) */
    private String content;

    /** 答案内容 */
    private String answer;

    /** 题目列表 */
    private List<QuestionItem> questions;

    /** 元信息(时间/作者等) */
    private Map<String, String> metaInfo;

    /** 质量评分(0-1) */
    private Double qualityScore;

    /** 解析是否成功 */
    private Boolean success;

    /** 错误信息 */
    private String errorMessage;

    @Data
    public static class QuestionItem implements Serializable {
        /** 题号 */
        private Integer questionNo;

        /** 题干 */
        private String questionText;

        /** 题型: single-单选 multiple-多选 fill-填空 */
        private String questionType;

        /** 选项列表 */
        private List<String> options;

        /** 答案 */
        private String answer;
    }
}

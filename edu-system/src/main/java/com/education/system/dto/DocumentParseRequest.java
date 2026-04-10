package com.education.system.dto;

import lombok.Data;

/**
 * 文档解析请求DTO
 */
@Data
public class DocumentParseRequest {

    /**
     * 文档类型: word-单词, question-题目, course-课程
     */
    private String documentType;

    /**
     * 业务类型: sys_word-单词训练, exam_question-题库
     */
    private String businessType;

    /**
     * 额外参数(如年级、难度等)
     */
    private String extraParams;
}

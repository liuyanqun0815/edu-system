package com.education.exam.dto;

import com.education.exam.entity.ExamPaper;
import lombok.Data;

import java.util.List;

/**
 * 试卷导出VO
 */
@Data
public class PaperExportVO {
    
    /**
     * 试卷信息
     */
    private ExamPaper paper;
    
    /**
     * 题目列表
     */
    private List<QuestionItem> questions;
    
    /**
     * 题目项
     */
    @Data
    public static class QuestionItem {
        private Long id;
        private Integer questionType;
        private Integer examType;
        private String questionTitle;
        private String images; // 题目图片URL列表
        private Integer imageCount;
        private String imageDescriptions; // 图片描述
        private String options;
        private Integer difficulty;
        private Integer score;
        private String correctAnswer;
        private String analysis;
    }
}

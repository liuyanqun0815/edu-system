package com.education.exam.dto;

import com.education.exam.entity.ExamQuestion;
import lombok.Data;

import java.util.List;

/**
 * 组卷结果VO
 */
@Data
public class PaperAssemblyResultVO {
    
    /**
     * 试卷ID
     */
    private Long paperId;
    
    /**
     * 总题数
     */
    private Integer totalCount;
    
    /**
     * 总分
     */
    private Integer totalScore;
    
    /**
     * 各难度统计
     */
    private DifficultyStats difficultyStats;
    
    /**
     * 已选题目列表
     */
    private List<ExamQuestion> selectedQuestions;
    
    /**
     * 各难度统计
     */
    @Data
    public static class DifficultyStats {
        private Integer easyCount;
        private Integer easyScore;
        private Integer mediumCount;
        private Integer mediumScore;
        private Integer hardCount;
        private Integer hardScore;
    }
}

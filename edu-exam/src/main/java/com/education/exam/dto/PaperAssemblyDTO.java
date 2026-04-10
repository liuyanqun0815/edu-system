package com.education.exam.dto;

import lombok.Data;

import java.util.List;

/**
 * 组卷请求DTO
 */
@Data
public class PaperAssemblyDTO {
    
    /**
     * 试卷ID
     */
    private Long paperId;
    
    /**
     * 学科ID
     */
    private Long subjectId;
    
    /**
     * 年级
     */
    private String grade;
    
    /**
     * 考试类型 1-单元测试 2-期中 3-期末 4-模拟考 5-真题 6-课后练习
     */
    private Integer examType;
    
    /**
     * 难度配置列表
     */
    private List<DifficultyConfig> difficultyConfigs;
    
    /**
     * 难度配置
     */
    @Data
    public static class DifficultyConfig {
        /**
         * 难度等级 1-简单 2-中等 3-困难
         */
        private Integer difficulty;
        
        /**
         * 题型 1-单选 2-多选 3-判断 4-填空 5-简答
         */
        private Integer questionType;
        
        /**
         * 考试类型 1-单元测试 2-期中 3-期末 4-模拟考 5-真题 6-课后练习
         */
        private Integer examType;
        
        /**
         * 抽取数量
         */
        private Integer count;
        
        /**
         * 每题分值
         */
        private Integer score;
    }
}

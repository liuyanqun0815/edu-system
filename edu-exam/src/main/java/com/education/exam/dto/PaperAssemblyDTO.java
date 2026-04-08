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
         * 抽取数量
         */
        private Integer count;
        
        /**
         * 每题分值
         */
        private Integer score;
    }
}

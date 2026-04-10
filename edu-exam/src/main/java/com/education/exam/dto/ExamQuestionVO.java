package com.education.exam.dto;

import lombok.Data;

/**
 * 考试题目VO
 */
@Data
public class ExamQuestionVO {
    
    /**
     * 题目ID
     */
    private Long questionId;
    
    /**
     * 题目标题
     */
    private String title;
    
    /**
     * 题目类型 1-单选 2-多选 3-判断 4-填空 5-简答
     */
    private Integer questionType;
    
    /**
     * 考试类型 1-单元测试 2-期中 3-期末 4-模拟考 5-真题 6-课后练习
     */
    private Integer examType;
    
    /**
     * 难度等级 1-简单 2-中等 3-困难
     */
    private Integer difficulty;
    
    /**
     * 题目分值
     */
    private Integer score;
    
    /**
     * 知识点标签(多个标签用逗号分隔)
     */
    private String tags;
    
    /**
     * 题目来源 1-手动录入 2-批量导入 3-AI生成
     */
    private Integer sourceType;
    
    /**
     * 用户答案
     */
    private String userAnswer;
    
    /**
     * 选项JSON（选择题时有值，格式：[{"label":"A","content":"..."}]）
     */
    private String optionsJson;
}

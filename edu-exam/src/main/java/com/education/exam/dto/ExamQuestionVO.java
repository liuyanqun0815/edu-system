package com.education.exam.dto;

import com.education.exam.entity.QuestionOption;
import lombok.Data;

import java.util.List;

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
     * 题目分值
     */
    private Integer score;
    
    /**
     * 用户答案
     */
    private String userAnswer;
    
    /**
     * 选项列表（选择题时有值）
     */
    private List<QuestionOption> options;
}

package com.education.exam.dto;

import com.education.exam.entity.ExamQuestion;
import com.education.exam.entity.ExamRecord;
import com.education.exam.entity.ExamRecordDetail;
import lombok.Data;

import java.util.List;

/**
 * 考试记录详情VO
 */
@Data
public class ExamRecordDetailVO {
    
    /**
     * 考试记录
     */
    private ExamRecord record;
    
    /**
     * 浏览器信息
     */
    private String browserInfo;
    
    /**
     * 切屏次数
     */
    private Integer screenSwitchCount;
    
    /**
     * 警告次数
     */
    private Integer warningCount;
    
    /**
     * 答题详情列表
     */
    private List<QuestionDetailItem> questions;
    
    /**
     * 总题数
     */
    private Integer totalCount;
    
    /**
     * 正确题数
     */
    private Long correctCount;
    
    /**
     * 错误题数
     */
    private Long wrongCount;
    
    /**
     * 题目详情项
     */
    @Data
    public static class QuestionDetailItem {
        /**
         * 答题详情
         */
        private ExamRecordDetail detail;
        
        /**
         * 题目信息
         */
        private ExamQuestion question;
        
        /**
         * 选项JSON（格式：[{"label":"A","content":"..."}]）
         */
        private String optionsJson;
    }
}

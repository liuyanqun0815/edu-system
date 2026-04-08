package com.education.admin.dto;

import lombok.Data;

/**
 * 考试统计 VO
 */
@Data
public class ExamStatsVO {
    
    /**
     * 总试卷数
     */
    private Long totalPapers;
    
    /**
     * 已发布试卷数
     */
    private Long publishedPapers;
    
    /**
     * 草稿试卷数
     */
    private Long draftPapers;
    
    /**
     * 总考试记录数
     */
    private Long totalRecords;
    
    /**
     * 已完成考试数
     */
    private Long completedRecords;
}

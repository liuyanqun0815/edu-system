package com.education.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.exam.dto.ExamRecordDetailVO;
import com.education.exam.entity.ExamRecord;

/**
 * 考试记录Service接口
 */
public interface IExamRecordService extends IService<ExamRecord> {
    
    /**
     * 获取考试记录详情（含答题详情）
     */
    ExamRecordDetailVO getRecordDetail(Long recordId);
}

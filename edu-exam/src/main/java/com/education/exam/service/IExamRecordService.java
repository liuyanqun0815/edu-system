package com.education.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.exam.dto.AntiCheatVO;
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

    /**
     * 记录切屏
     */
    void recordScreenSwitch(Long recordId);

    /**
     * 记录警告
     */
    void recordWarning(Long recordId);

    /**
     * 获取防作弊信息
     */
    AntiCheatVO getAntiCheatInfo(Long recordId);
}

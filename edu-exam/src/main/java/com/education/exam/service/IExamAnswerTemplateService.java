package com.education.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.exam.entity.ExamAnswerTemplate;

/**
 * 考试答题卡模板Service接口
 */
public interface IExamAnswerTemplateService extends IService<ExamAnswerTemplate> {

    /**
     * 根据试卷类型获取模板
     */
    ExamAnswerTemplate getByPaperType(Integer paperType);
}

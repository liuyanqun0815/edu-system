package com.education.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.exam.entity.ExamAnswerTemplate;
import com.education.exam.mapper.ExamAnswerTemplateMapper;
import com.education.exam.service.IExamAnswerTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 考试答题卡模板Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamAnswerTemplateServiceImpl extends ServiceImpl<ExamAnswerTemplateMapper, ExamAnswerTemplate> implements IExamAnswerTemplateService {

    @Override
    public ExamAnswerTemplate getByPaperType(Integer paperType) {
        LambdaQueryWrapper<ExamAnswerTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamAnswerTemplate::getPaperType, paperType)
               .eq(ExamAnswerTemplate::getStatus, 1);
        
        return getOne(wrapper);
    }
}

package com.education.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.exam.entity.ExamQuestion;
import com.education.exam.mapper.ExamQuestionMapper;
import com.education.exam.service.IExamQuestionService;
import org.springframework.stereotype.Service;

/**
 * 题库Service实现
 */
@Service
public class ExamQuestionServiceImpl extends ServiceImpl<ExamQuestionMapper, ExamQuestion> implements IExamQuestionService {
}

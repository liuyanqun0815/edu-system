package com.education.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.exam.entity.ExamSubject;
import com.education.exam.mapper.ExamSubjectMapper;
import com.education.exam.service.IExamSubjectService;
import org.springframework.stereotype.Service;

/**
 * 学科Service实现
 */
@Service
public class ExamSubjectServiceImpl extends ServiceImpl<ExamSubjectMapper, ExamSubject> implements IExamSubjectService {
}

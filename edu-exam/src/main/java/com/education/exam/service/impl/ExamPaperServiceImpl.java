package com.education.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.exam.entity.ExamPaper;
import com.education.exam.mapper.ExamPaperMapper;
import com.education.exam.service.IExamPaperService;
import org.springframework.stereotype.Service;

/**
 * 试卷Service实现
 */
@Service
public class ExamPaperServiceImpl extends ServiceImpl<ExamPaperMapper, ExamPaper> implements IExamPaperService {
}

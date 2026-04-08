package com.education.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.exam.entity.Question;

public interface QuestionService extends IService<Question> {
    
    void saveQuestionWithOptions(Question question);
}

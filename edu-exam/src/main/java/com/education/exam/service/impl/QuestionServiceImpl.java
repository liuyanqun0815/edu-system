package com.education.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.exam.entity.Question;
import com.education.exam.entity.QuestionOption;
import com.education.exam.mapper.QuestionMapper;
import com.education.exam.mapper.QuestionOptionMapper;
import com.education.exam.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Autowired
    private QuestionOptionMapper optionMapper;

    @Override
    @Transactional
    public void saveQuestionWithOptions(Question question) {
        save(question);
        
        List<QuestionOption> options = question.getOptions();
        if (options != null && !options.isEmpty()) {
            for (QuestionOption option : options) {
                option.setQuestionId(question.getId());
                optionMapper.insert(option);
            }
        }
    }
}

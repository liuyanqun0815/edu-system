package com.education.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.common.exception.BusinessException;
import com.education.exam.dto.ExamRecordDetailVO;
import com.education.exam.entity.ExamRecord;
import com.education.exam.entity.ExamRecordDetail;
import com.education.exam.entity.Question;
import com.education.exam.entity.QuestionOption;
import com.education.exam.mapper.ExamRecordDetailMapper;
import com.education.exam.mapper.ExamRecordMapper;
import com.education.exam.mapper.QuestionMapper;
import com.education.exam.mapper.QuestionOptionMapper;
import com.education.exam.service.IExamRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 考试记录Service实现
 */
@Service
public class ExamRecordServiceImpl extends ServiceImpl<ExamRecordMapper, ExamRecord> implements IExamRecordService {

    @Autowired
    private ExamRecordDetailMapper recordDetailMapper;
    
    @Autowired
    private QuestionMapper questionMapper;
    
    @Autowired
    private QuestionOptionMapper optionMapper;

    @Override
    public ExamRecordDetailVO getRecordDetail(Long recordId) {
        // 获取考试记录
        ExamRecord record = getById(recordId);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }

        ExamRecordDetailVO vo = new ExamRecordDetailVO();
        vo.setRecord(record);

        // 获取答题详情
        LambdaQueryWrapper<ExamRecordDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamRecordDetail::getRecordId, recordId);
        List<ExamRecordDetail> details = recordDetailMapper.selectList(wrapper);
        
        List<ExamRecordDetailVO.QuestionDetailItem> questionDetails = new ArrayList<>();
        for (ExamRecordDetail detail : details) {
            ExamRecordDetailVO.QuestionDetailItem item = new ExamRecordDetailVO.QuestionDetailItem();
            item.setDetail(detail);
            
            // 获取题目信息
            Question question = questionMapper.selectById(detail.getQuestionId());
            if (question != null) {
                item.setQuestion(question);
                
                // 获取选项
                LambdaQueryWrapper<QuestionOption> optionWrapper = new LambdaQueryWrapper<>();
                optionWrapper.eq(QuestionOption::getQuestionId, question.getId())
                        .orderByAsc(QuestionOption::getSort);
                List<QuestionOption> options = optionMapper.selectList(optionWrapper);
                item.setOptions(options);
            }
            
            questionDetails.add(item);
        }
        
        vo.setQuestions(questionDetails);
        vo.setTotalCount(details.size());
        vo.setCorrectCount(details.stream().filter(d -> d.getIsCorrect() == 1).count());
        vo.setWrongCount(details.stream().filter(d -> d.getIsCorrect() == 0).count());

        return vo;
    }
}

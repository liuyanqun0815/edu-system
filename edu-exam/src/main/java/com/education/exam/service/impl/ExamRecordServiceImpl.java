package com.education.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.common.exception.BusinessException;
import com.education.exam.dto.AntiCheatVO;
import com.education.exam.dto.ExamRecordDetailVO;
import com.education.exam.entity.ExamQuestion;
import com.education.exam.entity.ExamRecord;
import com.education.exam.entity.ExamRecordDetail;
import com.education.exam.mapper.ExamQuestionMapper;
import com.education.exam.mapper.ExamRecordDetailMapper;
import com.education.exam.mapper.ExamRecordMapper;
import com.education.exam.service.IExamRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 考试记录Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamRecordServiceImpl extends ServiceImpl<ExamRecordMapper, ExamRecord> implements IExamRecordService {

    @Autowired
    private ExamRecordDetailMapper recordDetailMapper;
    
    @Autowired
    private ExamQuestionMapper examQuestionMapper;

    @Override
    public ExamRecordDetailVO getRecordDetail(Long recordId) {
        // 获取考试记录
        ExamRecord record = getById(recordId);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }

        ExamRecordDetailVO vo = new ExamRecordDetailVO();
        vo.setRecord(record);
        
        // 防作弊信息
        vo.setBrowserInfo(record.getBrowserInfo());
        vo.setScreenSwitchCount(record.getScreenSwitchCount());
        vo.setWarningCount(record.getWarningCount());

        // 获取答题详情
        LambdaQueryWrapper<ExamRecordDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamRecordDetail::getRecordId, recordId);
        List<ExamRecordDetail> details = recordDetailMapper.selectList(wrapper);
        
        List<ExamRecordDetailVO.QuestionDetailItem> questionDetails = new ArrayList<>();
        for (ExamRecordDetail detail : details) {
            ExamRecordDetailVO.QuestionDetailItem item = new ExamRecordDetailVO.QuestionDetailItem();
            item.setDetail(detail);
            
            // 获取题目信息
            ExamQuestion question = examQuestionMapper.selectById(detail.getQuestionId());
            if (question != null) {
                item.setQuestion(question);
                item.setOptionsJson(question.getOptions());
            }
            
            questionDetails.add(item);
        }
        
        vo.setQuestions(questionDetails);
        vo.setTotalCount(details.size());
        vo.setCorrectCount(details.stream().filter(d -> d.getIsCorrect() == 1).count());
        vo.setWrongCount(details.stream().filter(d -> d.getIsCorrect() == 0).count());

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordScreenSwitch(Long recordId) {
        ExamRecord record = getById(recordId);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }
        
        Integer count = record.getScreenSwitchCount() == null ? 0 : record.getScreenSwitchCount();
        record.setScreenSwitchCount(count + 1);
        
        // 超过阈值自动警告
        if (count >= 3) {
            Integer warningCount = record.getWarningCount() == null ? 0 : record.getWarningCount();
            record.setWarningCount(warningCount + 1);
            log.warn("考试记录[{}]切屏次数: {}, 触发警告", recordId, count + 1);
        }
        
        updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordWarning(Long recordId) {
        ExamRecord record = getById(recordId);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }
        
        Integer count = record.getWarningCount() == null ? 0 : record.getWarningCount();
        record.setWarningCount(count + 1);
        
        updateById(record);
        log.warn("考试记录[{}]警告次数: {}", recordId, count + 1);
    }

    @Override
    public AntiCheatVO getAntiCheatInfo(Long recordId) {
        ExamRecord record = getById(recordId);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }
        
        AntiCheatVO vo = new AntiCheatVO();
        vo.setBrowserInfo(record.getBrowserInfo());
        vo.setScreenSwitchCount(record.getScreenSwitchCount());
        vo.setWarningCount(record.getWarningCount());
        
        return vo;
    }
}

package com.education.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.common.constants.BusinessConstants;
import com.education.course.entity.EduChapterProgress;
import com.education.course.mapper.EduChapterProgressMapper;
import com.education.course.service.IEduChapterProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 章节学习进度Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EduChapterProgressServiceImpl extends ServiceImpl<EduChapterProgressMapper, EduChapterProgress> implements IEduChapterProgressService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProgress(Long courseId, Long chapterId, Long studentId, BigDecimal progress) {
        EduChapterProgress record = getProgress(courseId, chapterId, studentId);
        
        if (record == null) {
            record = new EduChapterProgress();
            record.setCourseId(courseId);
            record.setChapterId(chapterId);
            record.setStudentId(studentId);
            record.setWatchDuration(0);
            record.setIsCompleted(0);
        }

        // 将百分比进度转换为秒数
        int watchSeconds = progress.multiply(new BigDecimal(BusinessConstants.PROGRESS_CONVERT_FACTOR))
                                   .intValue();
        record.setWatchDuration(watchSeconds);
        record.setLastWatchTime(new Date());
        
        if (record.getTotalDuration() == null) {
            record.setTotalDuration(BusinessConstants.DEFAULT_COURSE_DURATION); // 默认总时长60分钟
        }

        if (progress.compareTo(new BigDecimal("100")) >= 0) {
            completeChapter(courseId, chapterId, studentId);
        } else {
            saveOrUpdate(record);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeChapter(Long courseId, Long chapterId, Long studentId) {
        EduChapterProgress record = getProgress(courseId, chapterId, studentId);
        
        if (record == null) {
            record = new EduChapterProgress();
            record.setCourseId(courseId);
            record.setChapterId(chapterId);
            record.setStudentId(studentId);
        }

        record.setWatchDuration(record.getTotalDuration() != null ? record.getTotalDuration() : BusinessConstants.DEFAULT_COURSE_DURATION);
        record.setIsCompleted(1);
        record.setLastWatchTime(new Date());
        
        saveOrUpdate(record);
        log.info("学员[{}]完成章节[{}]学习", studentId, chapterId);
    }

    @Override
    public EduChapterProgress getProgress(Long courseId, Long chapterId, Long studentId) {
        LambdaQueryWrapper<EduChapterProgress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EduChapterProgress::getCourseId, courseId)
               .eq(EduChapterProgress::getChapterId, chapterId)
               .eq(EduChapterProgress::getStudentId, studentId);
        
        return getOne(wrapper);
    }
}

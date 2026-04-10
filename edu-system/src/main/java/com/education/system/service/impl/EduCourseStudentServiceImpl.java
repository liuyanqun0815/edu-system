package com.education.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.common.constants.BusinessConstants;
import com.education.common.constants.StatusConstants;
import com.education.common.exception.BusinessException;
import com.education.system.entity.EduCourseStudent;
import com.education.system.mapper.EduCourseStudentMapper;
import com.education.system.service.IEduCourseStudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 课程学员关联Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EduCourseStudentServiceImpl extends ServiceImpl<EduCourseStudentMapper, EduCourseStudent> implements IEduCourseStudentService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enrollCourse(Long courseId, Long studentId) {
        // 检查是否已报名
        LambdaQueryWrapper<EduCourseStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EduCourseStudent::getCourseId, courseId)
               .eq(EduCourseStudent::getStudentId, studentId);
        
        if (count(wrapper) > 0) {
            throw new BusinessException("您已报名该课程");
        }

        // 创建报名记录
        EduCourseStudent record = new EduCourseStudent();
        record.setCourseId(courseId);
        record.setStudentId(studentId);
        record.setEnrollTime(new Date());
        record.setProgress(BigDecimal.ZERO);
        record.setStatus(StatusConstants.LEARNING); // 学习中
        
        save(record);
        log.info("学员[{}]报名课程[{}]", studentId, courseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProgress(Long courseId, Long studentId, BigDecimal progress) {
        LambdaQueryWrapper<EduCourseStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EduCourseStudent::getCourseId, courseId)
               .eq(EduCourseStudent::getStudentId, studentId);
        
        EduCourseStudent record = getOne(wrapper);
        if (record == null) {
            throw new BusinessException("未找到报名记录");
        }

        record.setProgress(progress);
        if (record.getStartTime() == null) {
            record.setStartTime(new Date());
        }
        
        updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishCourse(Long courseId, Long studentId) {
        LambdaQueryWrapper<EduCourseStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EduCourseStudent::getCourseId, courseId)
               .eq(EduCourseStudent::getStudentId, studentId);
        
        EduCourseStudent record = getOne(wrapper);
        if (record == null) {
            throw new BusinessException("未找到报名记录");
        }

        record.setStatus(StatusConstants.COMPLETED); // 已完成
        record.setProgress(new BigDecimal("100"));
        record.setFinishTime(new Date());
        
        updateById(record);
        log.info("学员[{}]完成课程[{}]", studentId, courseId);
    }
}

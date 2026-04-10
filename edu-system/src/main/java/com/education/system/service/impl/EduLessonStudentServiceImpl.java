package com.education.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.system.entity.EduLessonStudent;
import com.education.system.mapper.EduLessonStudentMapper;
import com.education.system.service.IEduLessonStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 排课-学生关联Service实现类
 *
 * @author system
 * @since 2026-04-09
 */
@Service
@RequiredArgsConstructor
public class EduLessonStudentServiceImpl extends ServiceImpl<EduLessonStudentMapper, EduLessonStudent>
        implements IEduLessonStudentService {

    @Override
    public List<Long> getStudentIdsByLessonId(Long lessonId) {
        LambdaQueryWrapper<EduLessonStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EduLessonStudent::getLessonId, lessonId);

        return list(wrapper).stream()
                .map(EduLessonStudent::getStudentId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addStudentsToLesson(Long lessonId, List<Long> studentIds) {
        // 批量添加,忽略已存在的记录
        studentIds.forEach(studentId -> {
            LambdaQueryWrapper<EduLessonStudent> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(EduLessonStudent::getLessonId, lessonId)
                   .eq(EduLessonStudent::getStudentId, studentId);

            if (count(wrapper) == 0) {
                EduLessonStudent lessonStudent = new EduLessonStudent();
                lessonStudent.setLessonId(lessonId);
                lessonStudent.setStudentId(studentId);
                save(lessonStudent);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeStudentFromLesson(Long lessonId, Long studentId) {
        LambdaQueryWrapper<EduLessonStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EduLessonStudent::getLessonId, lessonId)
               .eq(EduLessonStudent::getStudentId, studentId);

        remove(wrapper);
    }
}

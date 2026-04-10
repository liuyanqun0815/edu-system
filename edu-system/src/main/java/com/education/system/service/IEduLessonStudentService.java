package com.education.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.system.entity.EduLessonStudent;

import java.util.List;

/**
 * 排课-学生关联Service接口
 *
 * @author system
 * @since 2026-04-09
 */
public interface IEduLessonStudentService extends IService<EduLessonStudent> {

    /**
     * 获取排课的学生列表
     *
     * @param lessonId 排课ID
     * @return 学生ID列表
     */
    List<Long> getStudentIdsByLessonId(Long lessonId);

    /**
     * 批量添加学生到排课
     *
     * @param lessonId 排课ID
     * @param studentIds 学生ID列表
     */
    void addStudentsToLesson(Long lessonId, List<Long> studentIds);

    /**
     * 从排课中移除学生
     *
     * @param lessonId 排课ID
     * @param studentId 学生ID
     */
    void removeStudentFromLesson(Long lessonId, Long studentId);
}

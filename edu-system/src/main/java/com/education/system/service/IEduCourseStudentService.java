package com.education.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.system.entity.EduCourseStudent;

/**
 * 课程学员关联Service接口
 */
public interface IEduCourseStudentService extends IService<EduCourseStudent> {

    /**
     * 报名课程
     */
    void enrollCourse(Long courseId, Long studentId);

    /**
     * 更新学习进度
     */
    void updateProgress(Long courseId, Long studentId, java.math.BigDecimal progress);

    /**
     * 完成课程
     */
    void finishCourse(Long courseId, Long studentId);
}

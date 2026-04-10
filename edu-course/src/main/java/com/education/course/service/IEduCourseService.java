package com.education.course.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.course.dto.CourseStatsVO;
import com.education.course.entity.EduCourse;
import com.education.course.query.CourseQuery;
import com.education.common.result.PageResult;

/**
 * 课程Service接口
 */
public interface IEduCourseService extends IService<EduCourse> {

    /**
     * 分页查询课程列表
     */
    PageResult<EduCourse> pageCourses(CourseQuery query);

    /**
     * 增加浏览次数
     */
    void incrementViewCount(Long courseId);

    /**
     * 增加学习人数
     */
    void incrementLearnCount(Long courseId);

    /**
     * 更新课程评分
     */
    void updateCourseRating(Long courseId, Double rating);

    /**
     * 获取课程统计数据
     */
    com.education.course.dto.CourseStatsVO getCourseStats();
}

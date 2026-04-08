package com.education.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.course.entity.EduCourse;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程Mapper
 */
@Mapper
public interface EduCourseMapper extends BaseMapper<EduCourse> {
}

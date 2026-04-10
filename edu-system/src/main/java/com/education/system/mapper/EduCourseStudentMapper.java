package com.education.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.system.entity.EduCourseStudent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程学员关联Mapper
 */
@Mapper
public interface EduCourseStudentMapper extends BaseMapper<EduCourseStudent> {
}

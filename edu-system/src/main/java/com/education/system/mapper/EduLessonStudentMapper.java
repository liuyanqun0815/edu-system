package com.education.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.system.entity.EduLessonStudent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 排课-学生关联Mapper接口
 *
 * @author system
 * @since 2026-04-09
 */
@Mapper
public interface EduLessonStudentMapper extends BaseMapper<EduLessonStudent> {
}

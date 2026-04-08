package com.education.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.course.entity.EduCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程分类Mapper
 */
@Mapper
public interface EduCategoryMapper extends BaseMapper<EduCategory> {
}

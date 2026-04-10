package com.education.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.course.entity.EduChapterProgress;
import org.apache.ibatis.annotations.Mapper;

/**
 * 章节学习进度Mapper
 */
@Mapper
public interface EduChapterProgressMapper extends BaseMapper<EduChapterProgress> {
}

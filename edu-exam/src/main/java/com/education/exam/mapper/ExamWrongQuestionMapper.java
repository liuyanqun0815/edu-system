package com.education.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.exam.entity.ExamWrongQuestion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 错题本Mapper接口
 *
 * @author system
 * @since 2026-04-09
 */
@Mapper
public interface ExamWrongQuestionMapper extends BaseMapper<ExamWrongQuestion> {
}

package com.education.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.exam.entity.QuestionOption;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface QuestionOptionMapper extends BaseMapper<QuestionOption> {
}

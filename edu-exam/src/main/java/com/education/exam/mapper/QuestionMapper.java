package com.education.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.exam.entity.Question;
import com.education.exam.entity.QuestionOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
    
    @Select("SELECT * FROM question_option WHERE question_id = #{questionId}")
    List<QuestionOption> selectOptions(@Param("questionId") Long questionId);
}

package com.education.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.exam.entity.ExamSubject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 学科Mapper
 */
@Mapper
public interface ExamSubjectMapper extends BaseMapper<ExamSubject> {

    /**
     * 按学科统计题目数量（GROUP BY 避免全表扫描）
     */
    @Select("SELECT subject_id AS subjectId, COUNT(*) AS questionCount " +
            "FROM exam_question WHERE status = 1 AND dr = 0 GROUP BY subject_id")
    List<Map<String, Object>> countBySubject();
}

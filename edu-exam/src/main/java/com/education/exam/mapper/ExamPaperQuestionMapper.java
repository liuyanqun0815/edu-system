package com.education.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.exam.entity.ExamPaperQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ExamPaperQuestionMapper extends BaseMapper<ExamPaperQuestion> {
    
    @Select("SELECT * FROM exam_paper_question WHERE paper_id = #{paperId}")
    List<ExamPaperQuestion> selectByPaperId(@Param("paperId") Long paperId);
    
    @Select("SELECT q.*, pq.score as paperScore, pq.question_order FROM exam_question q " +
            "JOIN exam_paper_question pq ON q.id = pq.question_id " +
            "WHERE pq.paper_id = #{paperId} ORDER BY pq.question_order")
    List<Map<String, Object>> selectQuestionsByPaperId(@Param("paperId") Long paperId);
}

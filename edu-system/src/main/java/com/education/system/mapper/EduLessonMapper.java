package com.education.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.system.entity.EduLesson;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface EduLessonMapper extends BaseMapper<EduLesson> {

    /**
     * 查询教师某日的排课列表（含学生数）
     */
    @Select("SELECT l.*, " +
            "(SELECT COUNT(*) FROM edu_lesson_student ls WHERE ls.lesson_id = l.id) AS studentCount " +
            "FROM edu_lesson l " +
            "WHERE l.teacher_id = #{teacherId} AND l.lesson_date = #{date} AND l.status = 1 " +
            "ORDER BY l.start_time")
    List<java.util.Map<String, Object>> selectTeacherLessons(@Param("teacherId") Long teacherId,
                                                              @Param("date") LocalDate date);

    /**
     * 查询学生某日参与的排课列表
     */
    @Select("SELECT l.* FROM edu_lesson l " +
            "INNER JOIN edu_lesson_student ls ON ls.lesson_id = l.id " +
            "WHERE ls.student_id = #{studentId} AND l.lesson_date = #{date} AND l.status = 1 " +
            "ORDER BY l.start_time")
    List<EduLesson> selectStudentLessons(@Param("studentId") Long studentId,
                                          @Param("date") LocalDate date);

    /**
     * 查询指定日期范围内未发送通知的课程（开始时间在30分钟内）
     */
    @Select("SELECT l.* FROM edu_lesson l " +
            "WHERE l.status = 1 AND l.notified = 0 " +
            "AND l.lesson_date = CURDATE() " +
            "AND TIMEDIFF(l.start_time, CURTIME()) BETWEEN '00:00:00' AND '00:30:00'")
    List<EduLesson> selectPendingNotifyLessons();

    /**
     * 查询某节课的学生ID列表
     */
    @Select("SELECT student_id FROM edu_lesson_student WHERE lesson_id = #{lessonId}")
    List<Long> selectStudentIds(@Param("lessonId") Long lessonId);

    /**
     * 查询日期范围内的所有课程
     */
    @Select("SELECT * FROM edu_lesson " +
            "WHERE lesson_date BETWEEN #{startDate} AND #{endDate} AND status = 1 " +
            "ORDER BY lesson_date, start_time")
    List<EduLesson> selectByDateRange(@Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);
}

package com.education.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.system.entity.SysTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface SysTaskMapper extends BaseMapper<SysTask> {

    /**
     * 查询用户某日任务
     */
    @Select("SELECT * FROM sys_task WHERE user_id = #{userId} AND task_date = #{date} ORDER BY start_time, priority DESC")
    List<SysTask> selectByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 查询用户日期范围内的任务
     */
    @Select("SELECT * FROM sys_task WHERE user_id = #{userId} AND task_date BETWEEN #{startDate} AND #{endDate} ORDER BY task_date, start_time, priority DESC")
    List<SysTask> selectByUserAndDateRange(@Param("userId") Long userId, 
                                           @Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);
}

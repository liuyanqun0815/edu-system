package com.education.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.system.entity.UserOnlineLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserOnlineLogMapper extends BaseMapper<UserOnlineLog> {
    
    @Select("SELECT COUNT(DISTINCT user_id) FROM user_online_log WHERE DATE(login_time) = CURDATE()")
    int countTodayLogin();
    
    @Select("SELECT COALESCE(SUM(duration), 0) FROM user_online_log WHERE user_id = #{userId}")
    int sumDurationByUserId(@Param("userId") Long userId);
    
    @Select("SELECT COALESCE(SUM(duration), 0) FROM user_online_log WHERE user_id = #{userId} AND DATE(login_time) = CURDATE()")
    int sumTodayDurationByUserId(@Param("userId") Long userId);
    
    @Select("SELECT COUNT(*) FROM user_online_log WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);
}

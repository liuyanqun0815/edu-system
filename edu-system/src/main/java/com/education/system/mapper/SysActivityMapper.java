package com.education.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.system.entity.SysActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 活动管理Mapper
 */
@Mapper
public interface SysActivityMapper extends BaseMapper<SysActivity> {

    /**
     * 查询已发布的活动列表（按置顶和排序）
     */
    @Select("SELECT * FROM sys_activity WHERE status = 1 ORDER BY is_top DESC, sort ASC, create_time DESC")
    List<SysActivity> selectPublishedList();

    /**
     * 查询最新的N条活动（按置顶和排序）
     */
    @Select("SELECT * FROM sys_activity WHERE status = 1 ORDER BY is_top DESC, sort ASC, create_time DESC LIMIT #{limit}")
    List<SysActivity> selectLatestList(@Param("limit") int limit);

    /**
     * 查询置顶的活动列表
     */
    @Select("SELECT * FROM sys_activity WHERE status = 1 AND is_top = 1 ORDER BY sort ASC, create_time DESC")
    List<SysActivity> selectTopList();

    /**
     * 根据角色ID查询活动列表
     */
    @Select("SELECT a.* FROM sys_activity a " +
            "LEFT JOIN sys_activity_role ar ON a.id = ar.activity_id " +
            "WHERE a.status = 1 AND (ar.role_id = #{roleId} OR ar.role_id IS NULL) " +
            "ORDER BY a.is_top DESC, a.sort ASC, a.create_time DESC LIMIT #{limit}")
    List<SysActivity> selectByRoleId(@Param("roleId") Long roleId, @Param("limit") int limit);

    /**
     * 按角色可见性查询：
     * - roleId为null：只返回未限定角色的活动（全员可见）
     * - roleId不为null：返回该角色可见的 + 全员可见的
     */
    @Select("<script>" +
            "SELECT DISTINCT a.* FROM sys_activity a " +
            "LEFT JOIN sys_activity_role ar ON a.id = ar.activity_id " +
            "WHERE a.status = 1 " +
            "<choose>" +
            "  <when test='roleId != null'>" +
            "    AND (ar.role_id = #{roleId} OR NOT EXISTS (SELECT 1 FROM sys_activity_role WHERE activity_id = a.id))" +
            "  </when>" +
            "  <otherwise>" +
            "    AND NOT EXISTS (SELECT 1 FROM sys_activity_role WHERE activity_id = a.id)" +
            "  </otherwise>" +
            "</choose>" +
            "ORDER BY a.is_top DESC, a.sort ASC, a.create_time DESC LIMIT #{limit}" +
            "</script>")
    List<SysActivity> selectVisibleActivities(@Param("roleId") Long roleId, @Param("limit") int limit);
}

package com.education.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.system.entity.SysActivityRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 活动角色推送关联Mapper
 */
@Mapper
public interface SysActivityRoleMapper extends BaseMapper<SysActivityRole> {

    /**
     * 根据活动ID查询角色ID列表
     */
    @Select("SELECT role_id FROM sys_activity_role WHERE activity_id = #{activityId}")
    List<Long> selectRoleIdsByActivityId(@Param("activityId") Long activityId);

    /**
     * 根据角色ID查询活动ID列表
     */
    @Select("SELECT activity_id FROM sys_activity_role WHERE role_id = #{roleId}")
    List<Long> selectActivityIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除活动的所有角色关联
     */
    @Select("DELETE FROM sys_activity_role WHERE activity_id = #{activityId}")
    void deleteByActivityId(@Param("activityId") Long activityId);
}

package com.education.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.system.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * 查询拥有指定角色ID列表中任意角色的用户ID列表
     */
    @Select("<script>" +
            "SELECT DISTINCT user_id FROM sys_user_role WHERE role_id IN " +
            "<foreach collection='roleIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<Long> selectUserIdsByRoleIds(@Param("roleIds") List<Long> roleIds);
}

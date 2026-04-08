package com.education.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.system.entity.SysUserMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户菜单关联Mapper
 */
@Mapper
public interface SysUserMenuMapper extends BaseMapper<SysUserMenu> {

    /**
     * 根据用户ID查询菜单ID列表
     */
    @Select("SELECT menu_id FROM sys_user_menu WHERE user_id = #{userId}")
    List<Long> selectMenuIdsByUserId(@Param("userId") Long userId);

    /**
     * 删除用户的所有菜单权限
     */
    @Delete("DELETE FROM sys_user_menu WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") Long userId);
    /**
     * 通过用户ID查询其所有角色拥有的菜单ID（去重）
     */
    @Select("SELECT DISTINCT rm.menu_id FROM sys_user_role ur " +
            "JOIN sys_role_menu rm ON ur.role_id = rm.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<Long> selectRoleMenuIdsByUserId(@Param("userId") Long userId);
}

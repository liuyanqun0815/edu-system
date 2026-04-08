package com.education.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.system.entity.SysMenuButton;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单按钮关系Mapper
 */
@Mapper
public interface SysMenuButtonMapper extends BaseMapper<SysMenuButton> {
    
    /**
     * 根据菜单ID查询关联的按钮ID列表
     */
    @Select("SELECT button_id FROM sys_menu_button WHERE menu_id = #{menuId}")
    List<Long> selectButtonIdsByMenuId(@Param("menuId") Long menuId);
    
    /**
     * 根据按钮ID查询关联的菜单ID列表
     */
    @Select("SELECT menu_id FROM sys_menu_button WHERE button_id = #{buttonId}")
    List<Long> selectMenuIdsByButtonId(@Param("buttonId") Long buttonId);
    
    /**
     * 删除菜单的所有按钮关联
     */
    @Select("DELETE FROM sys_menu_button WHERE menu_id = #{menuId}")
    void deleteByMenuId(@Param("menuId") Long menuId);
}

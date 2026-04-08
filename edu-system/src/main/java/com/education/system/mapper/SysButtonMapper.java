package com.education.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.system.entity.SysButton;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 按钮配置Mapper
 */
@Mapper
public interface SysButtonMapper extends BaseMapper<SysButton> {
    
    /**
     * 根据菜单ID查询关联的按钮列表
     */
    @Select("SELECT b.* FROM sys_button b " +
            "INNER JOIN sys_menu_button mb ON b.id = mb.button_id " +
            "WHERE mb.menu_id = #{menuId} AND b.status = 1 " +
            "ORDER BY b.id")
    List<SysButton> selectByMenuId(@Param("menuId") Long menuId);
    
    /**
     * 查询所有启用的按钮
     */
    @Select("SELECT * FROM sys_button WHERE status = 1 ORDER BY id")
    List<SysButton> selectAllActive();
}

package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 菜单按钮关系实体
 */
@Data
@TableName("sys_menu_button")
public class SysMenuButton {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 菜单ID
     */
    private Long menuId;
    
    /**
     * 按钮ID
     */
    private Long buttonId;
    
    /**
     * 创建时间
     */
    private Date createTime;
}

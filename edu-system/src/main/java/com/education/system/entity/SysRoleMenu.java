package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_role_menu")
public class SysRoleMenu {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long roleId;
    
    private Long menuId;
    
    private Date createTime;
}

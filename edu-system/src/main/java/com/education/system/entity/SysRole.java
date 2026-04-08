package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 系统角色实体类
 * 
 * <p>定义系统角色，用于权限控制和用户分类管理。</p>
 * 
 * <h3>角色等级说明：</h3>
 * <table border="1">
 *   <tr><th>roleLevel</th><th>角色名称</th><th>说明</th></tr>
 *   <tr><td>0</td><td>学生</td><td>普通学员，只能访问学习相关功能</td></tr>
 *   <tr><td>1</td><td>教师</td><td>可管理课程和学生</td></tr>
 *   <tr><td>2</td><td>管理员</td><td>系统管理，可管理教师和学生</td></tr>
 *   <tr><td>3</td><td>超级管理员</td><td>最高权限，可管理所有角色</td></tr>
 * </table>
 * 
 * <h3>权限控制机制：</h3>
 * <ul>
 *   <li><b>菜单权限</b>：通过SysRoleMenu关联控制可访问的菜单</li>
 *   <li><b>数据权限</b>：通过roleLevel控制可见数据范围</li>
 *   <li><b>按钮权限</b>：通过权限标识(permission)控制操作权限</li>
 * </ul>
 * 
 * <h3>关联关系：</h3>
 * <ul>
 *   <li>SysUser - 用户（多对多，通过SysUserRole）</li>
 *   <li>SysMenu - 菜单（多对多，通过SysRoleMenu）</li>
 * </ul>
 * 
 * @see SysUser 用户实体
 * @see SysUserRole 用户角色关联
 * @see SysRoleMenu 角色菜单关联
 * @author education-team
 */
@Data
@TableName("sys_role")
public class SysRole implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色名称
     */
    @TableField("name")
    private String roleName;

    /**
     * 角色编码
     */
    @TableField("code")
    private String roleCode;

    /**
     * 描述
     */
    private String description;

    /**
     * 用户数量
     */
    private Integer userCount;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 角色等级：0-学生 1-教师 2-管理员 3-超级管理员
     */
    private Integer roleLevel;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}

package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 系统菜单实体类
 * 
 * <p>存储菜单结构，支持树形层级结构，用于前端路由和权限控制。</p>
 * 
 * <h3>菜单类型说明：</h3>
 * <table border="1">
 *   <tr><th>menuType</th><th>类型</th><th>说明</th></tr>
 *   <tr><td>1</td><td>目录</td><td>菜单分组，不对应具体页面，如"系统管理"</td></tr>
 *   <tr><td>2</td><td>菜单</td><td>实际页面菜单，对应前端组件</td></tr>
 *   <tr><td>3</td><td>按钮</td><td>页面内的操作权限按钮，如"新增""删除"</td></tr>
 * </table>
 * 
 * <h3>树形结构：</h3>
 * <pre>
 * parentId=0 表示根节点
 * 根目录
 * ├── 系统管理(parentId=0)
 * │   ├── 用户管理(parentId=1, menuType=2)
 * │   │   ├── 新增用户(parentId=2, menuType=3, permission="user:add")
 * │   │   └── 删除用户(parentId=2, menuType=3, permission="user:delete")
 * │   └── 角色管理
 * └── 课程管理
 * </pre>
 * 
 * <h3>权限标识：</h3>
 * <p>按钮类型菜单的permission字段用于接口权限校验，格式："模块:操作"</p>
 * <ul>
 *   <li>user:add - 新增用户权限</li>
 *   <li>user:edit - 编辑用户权限</li>
 *   <li>user:delete - 删除用户权限</li>
 * </ul>
 * 
 * <h3>前端路由生成：</h3>
 * <p>前端根据菜单数据动态生成路由：</p>
 * <ul>
 *   <li>path - 路由路径</li>
 *   <li>component - 组件路径</li>
 *   <li>icon - 菜单图标</li>
 * </ul>
 * 
 * @see SysRoleMenu 角色菜单关联
 * @see SysUserMenu 用户菜单关联
 * @author education-team
 */
@Data
@TableName("sys_menu")
public class SysMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    @TableField("name")
    private String menuName;

    /**
     * 菜单类型 1-目录 2-菜单 3-按钮
     */
    private Integer menuType;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 权限标识（如 user:assign:role），按钮类型菜单必填
     */
    private String permission;

    /**
     * 排序
     */
    @TableField("sort")
    private Integer sortOrder;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 子菜单（非数据库字段）
     */
    @TableField(exist = false)
    private List<SysMenu> children;
}

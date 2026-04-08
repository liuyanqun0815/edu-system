package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 系统用户实体类
 * 
 * <p>存储用户账号信息，支持多种角色（学生、教师、管理员）的统一管理。</p>
 * 
 * <h3>用户角色体系：</h3>
 * <table border="1">
 *   <tr><th>角色</th><th>roleLevel</th><th>权限范围</th></tr>
 *   <tr><td>学生</td><td>0</td><td>只能查看自己的数据</td></tr>
 *   <tr><td>教师</td><td>1</td><td>可管理分配给自己的学生</td></tr>
 *   <tr><td>管理员</td><td>2</td><td>可管理所有非超级管理员用户</td></tr>
 *   <tr><td>超级管理员</td><td>3</td><td>全部权限</td></tr>
 * </table>
 * 
 * <h3>层级关系：</h3>
 * <p>通过 parentId 字段构建用户层级树：</p>
 * <pre>
 * 超级管理员 → 管理员 → 教师 → 学生
 * </pre>
 * <p>上级用户可以查看和管理其下级用户的数据。</p>
 * 
 * <h3>关键字段说明：</h3>
 * <ul>
 *   <li><b>username</b> - 登录账号，唯一，用于登录认证</li>
 *   <li><b>password</b> - BCrypt加密后的密码</li>
 *   <li><b>parentId</b> - 上级用户ID，用于层级权限控制</li>
 *   <li><b>gradeId</b> - 年级ID，学生用户需要设置</li>
 *   <li><b>isOnline</b> - 在线状态，用于在线用户统计</li>
 *   <li><b>onlineTime</b> - 累计在线时长（分钟）</li>
 * </ul>
 * 
 * <h3>关联实体：</h3>
 * <ul>
 *   <li>SysRole - 用户角色（多对多，通过SysUserRole）</li>
 *   <li>SysMenu - 用户菜单（多对多，通过SysUserMenu）</li>
 *   <li>Grade - 所属年级</li>
 * </ul>
 * 
 * @see SysRole 角色实体
 * @see SysUserRole 用户角色关联
 * @author education-team
 */
@Data
@TableName("sys_user")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 常用地址（用于通勤时间估算）
     */
    private String address;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 性别 0-未知 1-男 2-女
     */
    private Integer sex;

    /**
     * 年级ID
     */
    private Long gradeId;

    /**
     * 上级用户ID（用于层级关系：管理员->教师->学生）
     */
    private Long parentId;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 是否在线 0-否 1-是
     */
    private Integer isOnline;

    /**
     * 在线时长（分钟）
     */
    private Integer onlineTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}

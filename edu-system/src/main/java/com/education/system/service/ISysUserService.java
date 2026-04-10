package com.education.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.common.result.PageResult;
import com.education.system.dto.UserRoleVO;
import com.education.system.entity.SysUser;
import com.education.system.query.UserQuery;

import java.util.List;
import java.util.Set;

/**
 * 用户Service接口
 */
public interface ISysUserService extends IService<SysUser> {

    /**
     * 根据用户名查询用户
     */
    SysUser getByUsername(String username);

    /**
     * 用户登录
     */
    String login(String username, String password, String captchaKey, String captchaCode);

    /**
     * 用户注册
     */
    void register(String username, String password, String email);

    /**
     * 修改密码
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 重置密码
     */
    void resetPassword(Long userId, String newPassword);

    /**
     * 分页查询用户列表
     */
    PageResult<SysUser> pageList(com.education.system.query.SysUserQuery query);

    /**
     * 添加用户
     */
    boolean addUser(SysUser user);

    /**
     * 更新用户
     */
    boolean updateUser(SysUser user);

    /**
     * 删除用户
     */
    boolean deleteUser(Long id);

    /**
     * 更新个人资料
     */
    boolean updateProfile(SysUser user);

    /**
     * 获取用户角色等级
     */
    Integer getUserRoleLevel(Long userId);

    /**
     * 获取用户角色列表
     */
    List<UserRoleVO> getUserRoles(Long userId);

    /**
     * 获取可管理的用户ID列表
     */
    java.util.List<Long> getManageableUserIds(Long userId, boolean isTeacher);

    /**
     * 获取可见的用户ID列表
     */
    Set<Long> getVisibleUserIds(Long userId, Integer roleLevel);

    /**
     * 更新用户在线状态
     */
    void updateOnlineStatus(Long userId, boolean isOnline);

    /**
     * 统计在线用户数
     */
    int countOnlineUsers();

    /**
     * 获取在线用户列表
     */
    List<SysUser> getOnlineUserList();

    /**
     * 获取当前登录用户
     */
    SysUser getCurrentUser();
}

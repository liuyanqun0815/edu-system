package com.education.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.common.result.PageResult;
import com.education.system.dto.UserRoleVO;
import com.education.system.entity.SysUser;
import com.education.system.query.SysUserQuery;

import java.util.List;

/**
 * 用户Service接口
 */
public interface ISysUserService extends IService<SysUser> {

    /**
     * 分页查询用户列表
     */
    PageResult<SysUser> pageList(SysUserQuery query);

    /**
     * 根据用户名查询用户
     */
    SysUser getByUsername(String username);

    /**
     * 新增用户
     */
    boolean addUser(SysUser user);

    /**
     * 修改用户
     */
    boolean updateUser(SysUser user);

    /**
     * 删除用户
     */
    boolean deleteUser(Long id);

    /**
     * 更新用户在线状态
     */
    void updateOnlineStatus(Long userId, boolean isOnline);

    /**
     * 统计在线用户数量
     */
    int countOnlineUsers();

    /**
     * 获取在线用户列表
     */
    List<SysUser> getOnlineUserList();

    /**
     * 更新当前用户信息
     */
    boolean updateProfile(SysUser user);

    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码（null表示管理员直接重置）
     * @param newPassword 新密码
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 获取当前用户
     */
    SysUser getCurrentUser();

    /**
     * 获取用户角色信息
     * @param userId 用户ID
     * @return 角色信息列表
     */
    List<UserRoleVO> getUserRoles(Long userId);

    /**
     * 获取可管理的用户ID列表
     * @param currentUserId 当前用户ID
     * @param isTeacher 是否是老师角色
     * @return 可管理的用户ID列表
     */
    java.util.List<Long> getManageableUserIds(Long currentUserId, boolean isTeacher);

    /**
     * 获取用户的角色等级（取最高等级）
     */
    Integer getUserRoleLevel(Long userId);

    /**
     * 获取当前用户可见的所有用户ID集合
     */
    java.util.Set<Long> getVisibleUserIds(Long currentUserId, Integer roleLevel);
}

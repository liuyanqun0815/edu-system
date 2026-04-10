package com.education.system.service;

import com.education.system.entity.SysUser;

import java.util.List;

/**
 * 用户在线状态Service接口
 */
public interface IUserOnlineService {

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
     * 强制用户下线
     */
    void forceOffline(Long userId);
}

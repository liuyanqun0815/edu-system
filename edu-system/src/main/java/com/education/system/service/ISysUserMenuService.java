package com.education.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.system.entity.SysUserMenu;

import java.util.List;

/**
 * 用户菜单权限Service接口
 */
public interface ISysUserMenuService extends IService<SysUserMenu> {

    /**
     * 获取用户的菜单ID列表
     */
    List<Long> getUserMenuIds(Long userId);

    /**
     * 分配用户菜单权限（用户级别权限，优先级高于角色）
     */
    boolean assignUserMenus(Long userId, List<Long> menuIds);

    /**
     * 获取用户最终菜单列表（合并角色权限和用户权限，用户权限优先级更高）
     */
    List<Long> getUserFinalMenuIds(Long userId);
}

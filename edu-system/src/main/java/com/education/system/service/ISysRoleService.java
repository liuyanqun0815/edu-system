package com.education.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.system.entity.SysRole;

import java.util.List;

/**
 * 角色Service接口
 */
public interface ISysRoleService extends IService<SysRole> {

    /**
     * 获取角色的菜单ID列表
     */
    List<Long> getRoleMenuIds(Long roleId);

    /**
     * 分配角色菜单权限
     */
    boolean assignRoleMenus(Long roleId, List<Long> menuIds);
}

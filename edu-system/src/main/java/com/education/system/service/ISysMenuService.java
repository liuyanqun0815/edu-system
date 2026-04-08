package com.education.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.system.entity.SysMenu;

import java.util.List;

/**
 * 菜单Service接口
 */
public interface ISysMenuService extends IService<SysMenu> {

    /**
     * 根据用户ID查询菜单列表
     */
    List<SysMenu> getMenusByUserId(Long userId);

    /**
     * 获取菜单树形结构
     */
    List<SysMenu> getMenuTree();

    /**
     * 获取用户的按钮权限标识集合（menuType=3的permission字段）
     * 用户有独立权限时取用户权限，否则取角色权限
     */
    java.util.Set<String> getUserPermissions(Long userId);
}

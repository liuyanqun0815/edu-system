package com.education.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.system.entity.SysUserMenu;
import com.education.system.mapper.SysUserMenuMapper;
import com.education.system.service.ISysUserMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户菜单权限Service实现
 */
@Service
@RequiredArgsConstructor
public class SysUserMenuServiceImpl extends ServiceImpl<SysUserMenuMapper, SysUserMenu> implements ISysUserMenuService {

    private final SysUserMenuMapper userMenuMapper;

    @Override
    public List<Long> getUserMenuIds(Long userId) {
        return userMenuMapper.selectMenuIdsByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignUserMenus(Long userId, List<Long> menuIds) {
        // 先删除原有权限
        userMenuMapper.deleteByUserId(userId);

        // 添加新权限
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                SysUserMenu userMenu = new SysUserMenu();
                userMenu.setUserId(userId);
                userMenu.setMenuId(menuId);
                userMenuMapper.insert(userMenu);
            }
        }
        return true;
    }

    @Override
    public List<Long> getUserFinalMenuIds(Long userId) {
        // 获取用户直接分配的菜单权限
        List<Long> userMenus = getUserMenuIds(userId);
        // 获取用户角色的菜单权限
        List<Long> roleMenus = userMenuMapper.selectRoleMenuIdsByUserId(userId);

        // 合并权限（取并集）
        Set<Long> mergedMenus = new HashSet<>();
        if (userMenus != null && !userMenus.isEmpty()) {
            mergedMenus.addAll(userMenus);
        }
        if (roleMenus != null && !roleMenus.isEmpty()) {
            mergedMenus.addAll(roleMenus);
        }

        return new ArrayList<>(mergedMenus);
    }
}

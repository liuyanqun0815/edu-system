package com.education.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.system.entity.SysMenu;
import com.education.system.mapper.SysMenuMapper;
import com.education.system.mapper.SysUserMenuMapper;
import com.education.system.service.ISysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 菜单Service实现
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    private final SysUserMenuMapper userMenuMapper;

    /**
     * 重写save方法,清除菜单缓存
     */
    @Override
    @Caching(evict = {
        @CacheEvict(value = "menuCache", key = "'tree'"),
        @CacheEvict(value = "menuCache", allEntries = true)
    })
    public boolean save(SysMenu entity) {
        return super.save(entity);
    }

    /**
     * 重写updateById方法,清除菜单缓存
     */
    @Override
    @Caching(evict = {
        @CacheEvict(value = "menuCache", key = "'tree'"),
        @CacheEvict(value = "menuCache", allEntries = true)
    })
    public boolean updateById(SysMenu entity) {
        return super.updateById(entity);
    }

    /**
     * 重写removeById方法,清除菜单缓存
     */
    @Override
    @Caching(evict = {
        @CacheEvict(value = "menuCache", key = "'tree'"),
        @CacheEvict(value = "menuCache", allEntries = true)
    })
    public boolean removeById(java.io.Serializable id) {
        return super.removeById(id);
    }

    @Override
    @Cacheable(value = "menuCache", key = "#userId")
    public List<SysMenu> getMenusByUserId(Long userId) {
        return baseMapper.selectMenusByUserId(userId);
    }

    @Override
    @Cacheable(value = "menuCache", key = "'tree'")
    public List<SysMenu> getMenuTree() {
        List<SysMenu> allMenus = baseMapper.selectList(null);
        List<SysMenu> rootMenus = allMenus.stream()
                .filter(menu -> menu.getParentId() == null || menu.getParentId() == 0)
                .sorted(Comparator.comparingInt(m -> m.getSortOrder() == null ? 0 : m.getSortOrder()))
                .collect(Collectors.toList());
        for (SysMenu root : rootMenus) {
            List<SysMenu> children = allMenus.stream()
                    .filter(menu -> root.getId().equals(menu.getParentId()))
                    .sorted(Comparator.comparingInt(m -> m.getSortOrder() == null ? 0 : m.getSortOrder()))
                    .collect(Collectors.toList());
            root.setChildren(children);
        }
        return rootMenus;
    }

    @Override
    @Cacheable(value = "menuCache", key = "'perm:' + #userId")
    public Set<String> getUserPermissions(Long userId) {
        // 1. 先查用户是否有独立权限配置
        List<Long> userMenuIds = userMenuMapper.selectMenuIdsByUserId(userId);

        List<SysMenu> permMenus;
        if (userMenuIds != null && !userMenuIds.isEmpty()) {
            // 用户有独立权限配置：取用户直接分配的 menuType=3 菜单
            permMenus = baseMapper.selectList(
                    new LambdaQueryWrapper<SysMenu>()
                            .eq(SysMenu::getMenuType, 3)
                            .in(SysMenu::getId, userMenuIds)
                            .isNotNull(SysMenu::getPermission)
                            .ne(SysMenu::getPermission, "")
            );
        } else {
            // 用户无独立权限：取角色赋予的 menuType=3 菜单
            List<Long> roleMenuIds = userMenuMapper.selectRoleMenuIdsByUserId(userId);
            if (roleMenuIds == null || roleMenuIds.isEmpty()) {
                return new HashSet<>();
            }
            permMenus = baseMapper.selectList(
                    new LambdaQueryWrapper<SysMenu>()
                            .eq(SysMenu::getMenuType, 3)
                            .in(SysMenu::getId, roleMenuIds)
                            .isNotNull(SysMenu::getPermission)
                            .ne(SysMenu::getPermission, "")
            );
        }

        return permMenus.stream().map(SysMenu::getPermission).collect(Collectors.toSet());
    }
}

package com.education.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.system.entity.SysRole;
import com.education.system.entity.SysRoleMenu;
import com.education.system.mapper.SysRoleMapper;
import com.education.system.mapper.SysRoleMenuMapper;
import com.education.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色Service实现
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    private final SysRoleMenuMapper roleMenuMapper;

    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        return roleMenuMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRoleMenus(Long roleId, List<Long> menuIds) {
        // 先删除原有权限
        roleMenuMapper.deleteByRoleId(roleId);
        
        // 添加新权限
        if (menuIds != null && !menuIds.isEmpty()) {
            List<SysRoleMenu> list = new ArrayList<>();
            for (Long menuId : menuIds) {
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                list.add(roleMenu);
            }
            // 批量保存
            for (SysRoleMenu roleMenu : list) {
                roleMenuMapper.insert(roleMenu);
            }
        }
        return true;
    }
}

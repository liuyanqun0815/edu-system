package com.education.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.common.exception.BusinessException;
import com.education.common.result.PageResult;
import com.education.common.utils.SecurityUtils;
import com.education.system.dto.UserRoleVO;
import com.education.system.entity.SysRole;
import com.education.system.entity.SysUser;
import com.education.system.entity.SysUserRole;
import com.education.system.mapper.SysRoleMapper;
import com.education.system.mapper.SysUserMapper;
import com.education.system.mapper.SysUserRoleMapper;
import com.education.system.query.SysUserQuery;
import com.education.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * 
 * <p>提供用户管理的核心业务逻辑，包括CRUD、密码管理、权限查询等。</p>
 * 
 * <h3>核心功能：</h3>
 * <table border="1">
 *   <tr><th>功能</th><th>方法</th><th>说明</th></tr>
 *   <tr><td>分页查询</td><td>pageList</td><td>支持角色、状态筛选和层级权限过滤</td></tr>
 *   <tr><td>用户创建</td><td>addUser</td><td>校验唯一性、密码加密后保存</td></tr>
 *   <tr><td>用户更新</td><td>updateUser</td><td>校验唯一性（排除自身）</td></tr>
 *   <tr><td>密码管理</td><td>changePassword</td><td>支持旧密码校验</td></tr>
 *   <tr><td>权限查询</td><td>getVisibleUserIds</td><td>递归获取所有下级用户ID</td></tr>
 * </table>
 * 
 * <h3>数据权限控制：</h3>
 * <p>通过 visibleUserIds 实现层级数据权限：</p>
 * <ul>
 *   <li>学生：只能看到自己</li>
 *   <li>教师：可以看到分配给自己的学生</li>
 *   <li>管理员：可以看到所有下属用户</li>
 * </ul>
 * 
 * <h3>唯一性校验：</h3>
 * <ul>
 *   <li>username - 用户名唯一</li>
 *   <li>email - 邮箱唯一</li>
 *   <li>phone - 手机号唯一</li>
 * </ul>
 * 
 * @see ISysUserService 用户服务接口
 * @see SysUser 用户实体
 * @author education-team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;

    @Override
    public PageResult<SysUser> pageList(SysUserQuery query) {
        // 如果按角色过滤，先查出该角色的用户ID
        List<Long> roleUserIds = null;
        if (query.getRoleId() != null) {
            LambdaQueryWrapper<SysUserRole> roleWrapper = new LambdaQueryWrapper<>();
            roleWrapper.eq(SysUserRole::getRoleId, query.getRoleId());
            roleUserIds = userRoleMapper.selectList(roleWrapper)
                    .stream().map(SysUserRole::getUserId).collect(Collectors.toList());
            if (roleUserIds.isEmpty()) {
                return PageResult.of(0L, Collections.emptyList(), query.getPageNum(), query.getPageSize());
            }
        }

        // 处理可见用户ID集合（权限控制）
        Set<Long> visibleIds = query.getVisibleUserIds();
        // 如果角色用户ID和可见用户ID都有，取交集
        if (roleUserIds != null && visibleIds != null) {
            final List<Long> finalRoleUserIds = roleUserIds;
            visibleIds = visibleIds.stream().filter(finalRoleUserIds::contains).collect(java.util.stream.Collectors.toSet());
        } else if (roleUserIds != null) {
            visibleIds = new java.util.HashSet<>(roleUserIds);
        }

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        final java.util.Set<Long> finalVisibleIds = visibleIds;
        wrapper.like(StringUtils.isNotBlank(query.getUsername()), SysUser::getUsername, query.getUsername())
                .like(StringUtils.isNotBlank(query.getNickname()), SysUser::getNickname, query.getNickname())
                .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus())
                .in(finalVisibleIds != null && !finalVisibleIds.isEmpty(), SysUser::getId,
                        finalVisibleIds != null ? finalVisibleIds : Collections.emptySet())
                .orderByDesc(SysUser::getCreateTime);

        Page<SysUser> page = page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.getTotal(), page.getRecords(), query.getPageNum(), query.getPageSize());
    }

    @Override
    public SysUser getByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return getOne(wrapper);
    }

    @Override
    public boolean addUser(SysUser user) {
        // 检查用户名是否已存在
        if (getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, user.getUsername())) != null) {
            throw new BusinessException("用户名\"" + user.getUsername() + "\"已存在，请更换用户名");
        }
        // 检查邮箱唯一
        if (StringUtils.isNotBlank(user.getEmail())) {
            if (getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, user.getEmail())) != null) {
                throw new BusinessException("邮箱\"" + user.getEmail() + "\"已被其他用户使用，请更换邮箱");
            }
        }
        // 检查手机号唯一
        if (StringUtils.isNotBlank(user.getPhone())) {
            if (getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, user.getPhone())) != null) {
                throw new BusinessException("手机号\"" + user.getPhone() + "\"已被其他用户使用，请更换手机号");
            }
        }
        // 加密密码
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        return save(user);
    }

    @Override
    public boolean updateUser(SysUser user) {
        if (user.getId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        // 检查邮箱唯一（排除自己）
        if (StringUtils.isNotBlank(user.getEmail())) {
            SysUser exist = getOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getEmail, user.getEmail())
                    .ne(SysUser::getId, user.getId()));
            if (exist != null) {
                throw new BusinessException("邮箱\"" + user.getEmail() + "\"已被其他用户使用，请更换邮箱");
            }
        }
        // 检查手机号唯一（排除自己）
        if (StringUtils.isNotBlank(user.getPhone())) {
            SysUser exist = getOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getPhone, user.getPhone())
                    .ne(SysUser::getId, user.getId()));
            if (exist != null) {
                throw new BusinessException("手机号\"" + user.getPhone() + "\"已被其他用户使用，请更换手机号");
            }
        }
        // 不更新密码
        user.setPassword(null);
        return updateById(user);
    }

    @Override
    public boolean deleteUser(Long id) {
        return removeById(id);
    }

    @Override
    public void updateOnlineStatus(Long userId, boolean isOnline) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setIsOnline(isOnline ? 1 : 0);
        updateById(user);
    }

    @Override
    public int countOnlineUsers() {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getIsOnline, 1);
        return count(wrapper);
    }

    @Override
    public List<SysUser> getOnlineUserList() {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getIsOnline, 1);
        return list(wrapper);
    }

    @Override
    public boolean updateProfile(SysUser user) {
        if (user.getId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        // 检查邮箱唯一（排除自己）
        if (StringUtils.isNotBlank(user.getEmail())) {
            SysUser exist = getOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getEmail, user.getEmail())
                    .ne(SysUser::getId, user.getId()));
            if (exist != null) {
                throw new BusinessException("邮箱\"" + user.getEmail() + "\"已被其他用户使用，请更换邮箱");
            }
        }
        // 检查手机号唯一（排除自己）
        if (StringUtils.isNotBlank(user.getPhone())) {
            SysUser exist = getOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getPhone, user.getPhone())
                    .ne(SysUser::getId, user.getId()));
            if (exist != null) {
                throw new BusinessException("手机号\"" + user.getPhone() + "\"已被其他用户使用，请更换手机号");
            }
        }
        // 只允许更新特定字段
        SysUser updateUser = new SysUser();
        updateUser.setId(user.getId());
        updateUser.setNickname(user.getNickname());
        updateUser.setAvatar(user.getAvatar());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setSex(user.getSex());
        return updateById(updateUser);
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        if (userId == null) throw new BusinessException("用户ID不能为空");
        if (StringUtils.isBlank(newPassword)) throw new BusinessException("新密码不能为空");
        SysUser user = getById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        // 如果传了旧密码，必须校验
        if (StringUtils.isNotBlank(oldPassword)) {
            if (!SecurityUtils.matchesPassword(oldPassword, user.getPassword())) {
                throw new BusinessException("原密码不正确");
            }
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(SecurityUtils.encryptPassword(newPassword));
        return updateById(update);
    }

    @Override
    public SysUser getCurrentUser() {
        // 简化处理，返回ID为1的用户（admin）
        // 实际应该从SecurityContext获取当前登录用户
        return getById(1L);
    }

    @Override
    public List<UserRoleVO> getUserRoles(Long userId) {
        List<UserRoleVO> result = new ArrayList<>();
        
        // 获取用户的角色ID列表
        LambdaQueryWrapper<SysUserRole> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> userRoles = userRoleMapper.selectList(urWrapper);
        
        if (userRoles.isEmpty()) {
            return result;
        }
        
        // 获取角色详情
        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
        List<SysRole> roles = roleMapper.selectBatchIds(roleIds);
        
        for (SysRole role : roles) {
            UserRoleVO vo = new UserRoleVO();
            vo.setRoleId(role.getId());
            vo.setRoleName(role.getRoleName());
            vo.setRoleCode(role.getRoleCode());
            result.add(vo);
        }
        
        return result;
    }

    @Override
    public java.util.List<Long> getManageableUserIds(Long currentUserId, boolean isTeacher) {
        java.util.List<Long> result = new java.util.ArrayList<>();
        
        if (isTeacher) {
            // 老师：获取所有学生角色的用户
            // 查找学生角色
            LambdaQueryWrapper<SysRole> roleWrapper = new LambdaQueryWrapper<>();
            roleWrapper.like(SysRole::getRoleCode, "student");
            List<SysRole> studentRoles = roleMapper.selectList(roleWrapper);
            
            if (!studentRoles.isEmpty()) {
                List<Long> studentRoleIds = studentRoles.stream()
                        .map(SysRole::getId)
                        .collect(Collectors.toList());
                
                // 获取这些角色的用户
                LambdaQueryWrapper<SysUserRole> urWrapper = new LambdaQueryWrapper<>();
                urWrapper.in(SysUserRole::getRoleId, studentRoleIds);
                List<SysUserRole> userRoles = userRoleMapper.selectList(urWrapper);
                
                result = userRoles.stream()
                        .map(SysUserRole::getUserId)
                        .distinct()
                        .collect(Collectors.toList());
            }
        }
        // 管理员可以看所有非管理员用户，但这个逻辑在Controller中处理
        
        return result;
    }

    @Override
    public Integer getUserRoleLevel(Long userId) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> userRoles = userRoleMapper.selectList(wrapper);
        
        if (userRoles.isEmpty()) return 0;
        
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        List<SysRole> roles = roleMapper.selectBatchIds(roleIds);
        
        return roles.stream()
                .mapToInt(r -> r.getRoleLevel() != null ? r.getRoleLevel() : 0)
                .max()
                .orElse(0);
    }

    @Override
    public java.util.Set<Long> getVisibleUserIds(Long currentUserId, Integer roleLevel) {
        java.util.Set<Long> result = new java.util.HashSet<>();
        result.add(currentUserId); // 包含自己
        
        if (roleLevel == null || roleLevel == 0) {
            // 学生：只能看到自己
            return result;
        }
        
        // 获取所有用户（用于递归查询层级）
        List<SysUser> allUsers = list();
        
        // 递归获取所有下级用户ID
        collectSubordinateIds(currentUserId, allUsers, result);
        
        return result;
    }

    /**
     * 递归收集所有下级用户ID
     */
    private void collectSubordinateIds(Long parentId, List<SysUser> allUsers, java.util.Set<Long> result) {
        for (SysUser user : allUsers) {
            if (parentId.equals(user.getParentId())) {
                result.add(user.getId());
                collectSubordinateIds(user.getId(), allUsers, result);
            }
        }
    }
}
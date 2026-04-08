package com.education.system.controller;

import com.education.common.result.JsonResult;
import com.education.common.result.PageResult;
import com.education.common.utils.FileUploadUtil;
import com.education.system.entity.SysRole;
import com.education.system.entity.SysUser;
import com.education.system.entity.SysUserRole;
import com.education.system.mapper.SysRoleMapper;
import com.education.system.mapper.SysUserRoleMapper;
import com.education.system.query.SysUserQuery;
import com.education.system.service.ISysUserMenuService;
import com.education.system.service.ISysUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户管理Controller
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final ISysUserService userService;
    private final FileUploadUtil fileUploadUtil;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final ISysUserMenuService userMenuService;

    @ApiOperation("检查字段唯一性（邮箱、手机号）")
    @GetMapping("/check-unique")
    public JsonResult<Boolean> checkUnique(
            @RequestParam String field,
            @RequestParam String value,
            @RequestParam(required = false) Long excludeId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if ("email".equals(field)) {
            wrapper.eq(SysUser::getEmail, value);
        } else if ("phone".equals(field)) {
            wrapper.eq(SysUser::getPhone, value);
        } else if ("username".equals(field)) {
            wrapper.eq(SysUser::getUsername, value);
        } else {
            return JsonResult.success(true);
        }
        if (excludeId != null) {
            wrapper.ne(SysUser::getId, excludeId);
        }
        boolean isUnique = userService.count(wrapper) == 0;
        return JsonResult.success(isUnique);
    }

    @ApiOperation("分页查询用户列表（支持层级权限控制）")
    @GetMapping("/page")
    public JsonResult<PageResult<SysUser>> page(
            SysUserQuery query,
            @RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
        
        // 根据当前用户权限设置查询条件
        if (currentUserId != null) {
            Integer roleLevel = userService.getUserRoleLevel(currentUserId);
            
            if (roleLevel == null || roleLevel < 3) {
                // 非超级管理员：只能看自己和下级
                Set<Long> visibleUserIds = userService.getVisibleUserIds(currentUserId, roleLevel);
                if (visibleUserIds != null && !visibleUserIds.isEmpty()) {
                    query.setVisibleUserIds(visibleUserIds);
                }
            }
            // 超级管理员(roleLevel>=3)：不设限制，可以看所有
        }
        
        PageResult<SysUser> result = userService.pageList(query);
        return JsonResult.success(result);
    }

    @ApiOperation("获取当前登录用户信息")
    @GetMapping("/info")
    public JsonResult<SysUser> getCurrentUserInfo(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Long uid = userId != null ? userId : 1L;
        SysUser user = userService.getById(uid);
        return JsonResult.success(user);
    }

    @ApiOperation("根据ID查询用户")
    @GetMapping("/{id}")
    public JsonResult<SysUser> getById(@PathVariable Long id) {
        SysUser user = userService.getById(id);
        return JsonResult.success(user);
    }

    @ApiOperation("新增用户")
    @PostMapping
    public JsonResult<Void> add(@RequestBody @Validated SysUser user) {
        userService.addUser(user);
        return JsonResult.success("新增成功");
    }

    @ApiOperation("修改用户")
    @PutMapping
    public JsonResult<Void> update(@RequestBody @Validated SysUser user) {
        userService.updateUser(user);
        return JsonResult.success("修改成功");
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/{id}")
    public JsonResult<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return JsonResult.success("删除成功");
    }

    @ApiOperation("上传头像(base64)")
    @PostMapping("/upload-avatar")
    public JsonResult<Map<String, String>> uploadAvatar(@RequestBody Map<String, String> params) {
        String base64Image = params.get("image");
        if (base64Image == null || base64Image.isEmpty()) {
            throw new RuntimeException("图片数据不能为空");
        }
        
        // 验证base64格式
        if (!base64Image.startsWith("data:image")) {
            throw new RuntimeException("无效的图片格式");
        }
        
        // 检查图片大小（base64字符串长度限制，约2MB）
        if (base64Image.length() > 3 * 1024 * 1024) {
            throw new RuntimeException("图片大小不能超过2MB");
        }
        
        Map<String, String> result = new HashMap<>();
        result.put("url", base64Image);
        return JsonResult.success(result);
    }

    @ApiOperation("更新当前用户信息")
    @PutMapping("/profile")
    public JsonResult<Void> updateProfile(@RequestBody SysUser user) {
        userService.updateProfile(user);
        return JsonResult.success("更新成功");
    }

    @ApiOperation("修改密码")
    @PutMapping("/password")
    public JsonResult<Void> changePassword(@RequestHeader(value = "X-User-Id", required = false) Long currentUserId,
                                           @RequestBody Map<String, String> params) {
        String userIdStr = params.get("userId");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("新密码不能为空");
        }
        // 管理员重置他人密码：userIdStr != null 且不是自己
        Long targetUserId;
        if (userIdStr != null && !userIdStr.isEmpty()) {
            targetUserId = Long.parseLong(userIdStr);
        } else {
            targetUserId = currentUserId;
        }
        if (targetUserId == null) throw new RuntimeException("无法确定目标用户");
        // 如果是自己改密码，必须验证旧密码
        boolean isSelf = targetUserId.equals(currentUserId);
        userService.changePassword(targetUserId, isSelf ? oldPassword : null, newPassword);
        return JsonResult.success("密码修改成功");
    }

    @ApiOperation("获取当前用户信息")
    @GetMapping("/profile")
    public JsonResult<SysUser> getProfile(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                          @RequestParam(required = false) Long id) {
        Long uid = userId != null ? userId : id;
        if (uid == null || uid <= 0) {
            // 如果没有传入 userId，默认返回第一个用户
            uid = 1L;
        }
        SysUser user = userService.getById(uid);
        return JsonResult.success(user);
    }

    @ApiOperation("获取用户的角色ID列表")
    @GetMapping("/{userId}/roles")
    public JsonResult<List<Long>> getUserRoles(@PathVariable Long userId) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> list = userRoleMapper.selectList(wrapper);
        List<Long> roleIds = list.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        return JsonResult.success(roleIds);
    }

    @ApiOperation("分配用户角色")
    @PostMapping("/{userId}/roles")
    @Transactional(rollbackFor = Exception.class)
    public JsonResult<Void> assignUserRoles(@PathVariable Long userId,
                                            @RequestBody Map<String, List<Long>> params) {
        List<Long> roleIds = params.get("roleIds");
        // 先删除原有角色
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        userRoleMapper.delete(wrapper);
        // 插入新角色
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }
        return JsonResult.success("角色分配成功");
    }

    @ApiOperation("获取用户的菜单权限ID列表")
    @GetMapping("/{userId}/menus")
    public JsonResult<List<Long>> getUserMenus(@PathVariable Long userId) {
        List<Long> menuIds = userMenuService.getUserMenuIds(userId);
        return JsonResult.success(menuIds);
    }

    @ApiOperation("获取用户最终可见菜单（用户直接权限优先，无则取角色权限）")
    @GetMapping("/{userId}/menus/final")
    public JsonResult<List<Long>> getUserFinalMenus(@PathVariable Long userId) {
        List<Long> menuIds = userMenuService.getUserFinalMenuIds(userId);
        return JsonResult.success(menuIds);
    }

    @ApiOperation("分配用户菜单权限")
    @PostMapping("/{userId}/menus")
    public JsonResult<Void> assignUserMenus(@PathVariable Long userId,
                                            @RequestBody Map<String, List<Long>> params) {
        List<Long> menuIds = params.get("menuIds");
        userMenuService.assignUserMenus(userId, menuIds);
        return JsonResult.success("权限分配成功");
    }

    @ApiOperation("获取可选择的上级用户（只能选高一级角色）")
    @GetMapping("/selectable-parents")
    public JsonResult<List<Map<String, Object>>> getSelectableParents(
            @RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
        
        // 获取当前用户的角色等级
        Integer currentRoleLevel = userService.getUserRoleLevel(currentUserId);
        if (currentRoleLevel == null) {
            currentRoleLevel = 0;
        }
        
        // 获取所有角色，建立 roleId -> roleLevel 映射
        List<SysRole> allRoles = roleMapper.selectList(null);
        Map<Long, Integer> roleLevelMap = allRoles.stream()
                .collect(Collectors.toMap(SysRole::getId, r -> r.getRoleLevel() != null ? r.getRoleLevel() : 0));
        
        // 获取所有用户
        List<SysUser> allUsers = userService.list();
        
        // 获取所有用户的角色
        List<SysUserRole> allUserRoles = userRoleMapper.selectList(null);
        Map<Long, Integer> userRoleLevelMap = new HashMap<>();
        for (SysUserRole ur : allUserRoles) {
            Integer level = roleLevelMap.getOrDefault(ur.getRoleId(), 0);
            // 取用户最高角色等级
            userRoleLevelMap.merge(ur.getUserId(), level, Math::max);
        }
        
        // 筛选出比当前用户高一级或更高级别的用户
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysUser user : allUsers) {
            if (user.getId().equals(currentUserId)) continue; // 排除自己
            
            Integer userRoleLevel = userRoleLevelMap.getOrDefault(user.getId(), 0);
            // 只能选比自己高一级或更高级别的用户作为上级
            if (userRoleLevel > currentRoleLevel) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", user.getId());
                map.put("username", user.getUsername());
                map.put("nickname", user.getNickname());
                map.put("roleLevel", userRoleLevel);
                result.add(map);
            }
        }
        
        // 按角色等级排序（高等级在前）
        result.sort((a, b) -> (Integer) b.get("roleLevel") - (Integer) a.get("roleLevel"));
        
        return JsonResult.success(result);
    }

    @ApiOperation("获取教师列表（roleLevel >= 1）")
    @GetMapping("/teachers")
    public JsonResult<List<Map<String, Object>>> getTeachers() {
        // 获取所有角色，建立 roleId -> roleLevel 映射
        List<SysRole> allRoles = roleMapper.selectList(null);
        Map<Long, Integer> roleLevelMap = allRoles.stream()
                .collect(Collectors.toMap(SysRole::getId, r -> r.getRoleLevel() != null ? r.getRoleLevel() : 0));
        
        // 获取所有用户
        List<SysUser> allUsers = userService.list();
        
        // 获取所有用户的角色
        List<SysUserRole> allUserRoles = userRoleMapper.selectList(null);
        Map<Long, Integer> userRoleLevelMap = new HashMap<>();
        for (SysUserRole ur : allUserRoles) {
            Integer level = roleLevelMap.getOrDefault(ur.getRoleId(), 0);
            // 取用户最高角色等级
            userRoleLevelMap.merge(ur.getUserId(), level, Math::max);
        }
        
        // 筛选出教师及以上角色的用户（roleLevel >= 1）
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysUser user : allUsers) {
            Integer userRoleLevel = userRoleLevelMap.getOrDefault(user.getId(), 0);
            // 教师及以上角色（roleLevel >= 1）
            if (userRoleLevel >= 1) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", user.getId());
                map.put("username", user.getUsername());
                map.put("nickname", user.getNickname());
                map.put("roleLevel", userRoleLevel);
                result.add(map);
            }
        }
        
        // 按角色等级排序
        result.sort((a, b) -> (Integer) a.get("roleLevel") - (Integer) b.get("roleLevel"));
        
        return JsonResult.success(result);
    }
}

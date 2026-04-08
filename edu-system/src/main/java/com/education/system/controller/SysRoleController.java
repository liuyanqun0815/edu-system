package com.education.system.controller;

import com.education.common.result.JsonResult;
import com.education.system.entity.SysRole;
import com.education.system.service.ISysRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 角色管理Controller
 */
@Api(tags = "角色管理")
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final ISysRoleService roleService;

    @ApiOperation("查询角色列表")
    @GetMapping("/list")
    public JsonResult<List<SysRole>> list() {
        List<SysRole> list = roleService.list();
        return JsonResult.success(list);
    }

    @ApiOperation("根据ID查询角色")
    @GetMapping("/{id}")
    public JsonResult<SysRole> getById(@PathVariable Long id) {
        SysRole role = roleService.getById(id);
        return JsonResult.success(role);
    }

    @ApiOperation("新增角色")
    @PostMapping
    public JsonResult<Void> add(@RequestBody @Validated SysRole role) {
        roleService.save(role);
        return JsonResult.success("新增成功");
    }

    @ApiOperation("修改角色")
    @PutMapping
    public JsonResult<Void> update(@RequestBody @Validated SysRole role) {
        roleService.updateById(role);
        return JsonResult.success("修改成功");
    }

    @ApiOperation("删除角色")
    @DeleteMapping("/{id}")
    public JsonResult<Void> delete(@PathVariable Long id) {
        roleService.removeById(id);
        return JsonResult.success("删除成功");
    }

    @ApiOperation("获取角色的菜单ID列表")
    @GetMapping("/{roleId}/menus")
    public JsonResult<List<Long>> getRoleMenus(@PathVariable Long roleId) {
        List<Long> menuIds = roleService.getRoleMenuIds(roleId);
        return JsonResult.success(menuIds);
    }

    @ApiOperation("分配角色菜单权限")
    @PostMapping("/{roleId}/menus")
    public JsonResult<Void> assignRoleMenus(@PathVariable Long roleId, @RequestBody Map<String, List<Long>> params) {
        List<Long> menuIds = params.get("menuIds");
        roleService.assignRoleMenus(roleId, menuIds);
        return JsonResult.success("权限分配成功");
    }
}

package com.education.system.controller;

import com.education.common.result.JsonResult;
import com.education.system.entity.SysMenu;
import com.education.system.service.ISysMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 菜单管理Controller
 */
@Api(tags = "菜单管理")
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final ISysMenuService menuService;

    @ApiOperation("查询当前用户按钮权限集合")
    @GetMapping("/permissions")
    public JsonResult<Set<String>> getPermissions(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) return JsonResult.success(java.util.Collections.emptySet());
        Set<String> permissions = menuService.getUserPermissions(userId);
        return JsonResult.success(permissions);
    }

    @ApiOperation("查询菜单列表")
    @GetMapping("/list")
    public JsonResult<List<SysMenu>> list() {
        List<SysMenu> list = menuService.list();
        return JsonResult.success(list);
    }

    @ApiOperation("查询菜单树形结构")
    @GetMapping("/tree")
    public JsonResult<List<SysMenu>> tree() {
        List<SysMenu> tree = menuService.getMenuTree();
        return JsonResult.success(tree);
    }

    @ApiOperation("根据用户ID查询菜单")
    @GetMapping("/user/{userId}")
    public JsonResult<List<SysMenu>> getMenusByUserId(@PathVariable Long userId) {
        List<SysMenu> list = menuService.getMenusByUserId(userId);
        return JsonResult.success(list);
    }

    @ApiOperation("根据ID查询菜单")
    @GetMapping("/{id}")
    public JsonResult<SysMenu> getById(@PathVariable Long id) {
        SysMenu menu = menuService.getById(id);
        return JsonResult.success(menu);
    }

    @ApiOperation("新增菜单")
    @PostMapping
    public JsonResult<Void> add(@RequestBody @Validated SysMenu menu) {
        menuService.save(menu);
        return JsonResult.success("新增成功");
    }

    @ApiOperation("修改菜单")
    @PutMapping
    public JsonResult<Void> update(@RequestBody @Validated SysMenu menu) {
        menuService.updateById(menu);
        return JsonResult.success("修改成功");
    }

    @ApiOperation("删除菜单")
    @DeleteMapping("/{id}")
    public JsonResult<Void> delete(@PathVariable Long id) {
        menuService.removeById(id);
        return JsonResult.success("删除成功");
    }
}

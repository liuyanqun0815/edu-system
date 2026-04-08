package com.education.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.common.result.JsonResult;
import com.education.system.entity.SysParent;
import com.education.system.entity.SysUserParent;
import com.education.system.mapper.SysParentMapper;
import com.education.system.mapper.SysUserParentMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 家长信息Controller
 */
@Api(tags = "家长管理")
@RestController
@RequestMapping("/system/parent")
@RequiredArgsConstructor
public class SysParentController {

    private final SysParentMapper parentMapper;
    private final SysUserParentMapper userParentMapper;

    /**
     * 查询某学生的关联家长列表
     */
    @ApiOperation("查询学生的家长列表")
    @GetMapping("/by-student/{studentId}")
    public JsonResult<List<SysParent>> getByStudent(@PathVariable Long studentId) {
        return JsonResult.success(parentMapper.selectByStudentId(studentId));
    }

    /**
     * 新增家长并关联到学生
     */
    @ApiOperation("新增家长并关联")
    @PostMapping
    public JsonResult<Void> addAndBind(@RequestBody Map<String, Object> params) {
        Long studentId = params.get("studentId") != null
                ? Long.parseLong(params.get("studentId").toString()) : null;
        if (studentId == null) throw new RuntimeException("studentId不能为空");

        SysParent parent = new SysParent();
        parent.setName((String) params.get("name"));
        parent.setPhone((String) params.get("phone"));
        parent.setWechat((String) params.get("wechat"));
        parent.setEmail((String) params.get("email"));
        parent.setRelation((String) params.get("relation"));
        parent.setRemark((String) params.get("remark"));
        parentMapper.insert(parent);

        // 建立关联
        SysUserParent rel = new SysUserParent();
        rel.setUserId(studentId);
        rel.setParentId(parent.getId());
        userParentMapper.insert(rel);

        return JsonResult.success("关联成功");
    }

    /**
     * 解绑学生与家长的关联
     */
    @ApiOperation("解绑家长关联")
    @DeleteMapping("/unbind")
    public JsonResult<Void> unbind(@RequestParam Long userId, @RequestParam Long parentId) {
        LambdaQueryWrapper<SysUserParent> wrapper = new LambdaQueryWrapper<SysUserParent>()
                .eq(SysUserParent::getUserId, userId)
                .eq(SysUserParent::getParentId, parentId);
        userParentMapper.delete(wrapper);
        return JsonResult.success("解绑成功");
    }

    /**
     * 编辑家长信息
     */
    @ApiOperation("编辑家长信息")
    @PutMapping
    public JsonResult<Void> update(@RequestBody SysParent parent) {
        parentMapper.updateById(parent);
        return JsonResult.success("修改成功");
    }

    /**
     * 删除家长（同时删除所有关联）
     */
    @ApiOperation("删除家长")
    @DeleteMapping("/{id}")
    public JsonResult<Void> delete(@PathVariable Long id) {
        // 删除关联
        LambdaQueryWrapper<SysUserParent> wrapper = new LambdaQueryWrapper<SysUserParent>()
                .eq(SysUserParent::getParentId, id);
        userParentMapper.delete(wrapper);
        // 删除家长
        parentMapper.deleteById(id);
        return JsonResult.success("删除成功");
    }
}

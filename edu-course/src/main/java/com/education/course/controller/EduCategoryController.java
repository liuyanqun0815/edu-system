package com.education.course.controller;

import com.education.common.result.JsonResult;
import com.education.course.entity.EduCategory;
import com.education.course.service.IEduCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程分类Controller
 */
@Api(tags = "课程分类")
@RestController
@RequestMapping("/course/category")
@RequiredArgsConstructor
public class EduCategoryController {

    private final IEduCategoryService categoryService;

    @ApiOperation("查询分类树")
    @GetMapping("/tree")
    public JsonResult<List<EduCategory>> tree() {
        List<EduCategory> list = categoryService.treeList();
        return JsonResult.success(list);
    }

    @ApiOperation("查询分类列表")
    @GetMapping("/list")
    public JsonResult<List<EduCategory>> list() {
        List<EduCategory> list = categoryService.list();
        return JsonResult.success(list);
    }

    @ApiOperation("根据ID查询分类")
    @GetMapping("/{id}")
    public JsonResult<EduCategory> getById(@PathVariable Long id) {
        EduCategory category = categoryService.getById(id);
        return JsonResult.success(category);
    }

    @ApiOperation("新增分类")
    @PostMapping
    public JsonResult<Void> add(@RequestBody @Validated EduCategory category) {
        categoryService.save(category);
        return JsonResult.success("新增成功");
    }

    @ApiOperation("修改分类")
    @PutMapping
    public JsonResult<Void> update(@RequestBody @Validated EduCategory category) {
        categoryService.updateById(category);
        return JsonResult.success("修改成功");
    }

    @ApiOperation("删除分类")
    @DeleteMapping("/{id}")
    public JsonResult<Void> delete(@PathVariable Long id) {
        categoryService.removeById(id);
        return JsonResult.success("删除成功");
    }
}

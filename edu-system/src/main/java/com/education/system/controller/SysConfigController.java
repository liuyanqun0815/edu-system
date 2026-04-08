package com.education.system.controller;

import com.education.common.result.JsonResult;
import com.education.common.result.PageResult;
import com.education.system.dto.ConfigCategoryVO;
import com.education.system.entity.SysConfigCategory;
import com.education.system.entity.SysConfigItem;
import com.education.system.service.ISysConfigCategoryService;
import com.education.system.service.ISysConfigItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置管理Controller
 */
@Api(tags = "配置管理")
@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
public class SysConfigController {

    private final ISysConfigCategoryService categoryService;
    private final ISysConfigItemService itemService;

    // ==================== 配置分类接口 ====================

    @ApiOperation("查询所有配置分类")
    @GetMapping("/category/list")
    public JsonResult<List<SysConfigCategory>> listCategories() {
        List<SysConfigCategory> list = categoryService.listActiveCategories();
        return JsonResult.success(list);
    }

    @ApiOperation("根据ID查询配置分类")
    @GetMapping("/category/{id}")
    public JsonResult<SysConfigCategory> getCategoryById(@PathVariable Long id) {
        SysConfigCategory category = categoryService.getById(id);
        return JsonResult.success(category);
    }

    @ApiOperation("新增配置分类")
    @PostMapping("/category")
    public JsonResult<Void> addCategory(@RequestBody @Validated SysConfigCategory category) {
        categoryService.save(category);
        return JsonResult.success("新增成功");
    }

    @ApiOperation("修改配置分类")
    @PutMapping("/category")
    public JsonResult<Void> updateCategory(@RequestBody @Validated SysConfigCategory category) {
        categoryService.updateById(category);
        return JsonResult.success("修改成功");
    }

    @ApiOperation("删除配置分类")
    @DeleteMapping("/category/{id}")
    public JsonResult<Void> deleteCategory(@PathVariable Long id) {
        categoryService.removeById(id);
        return JsonResult.success("删除成功");
    }

    // ==================== 配置项接口 ====================

    @ApiOperation("根据分类编码查询配置项")
    @GetMapping("/item/list/{categoryCode}")
    public JsonResult<List<SysConfigItem>> listItemsByCategory(@PathVariable String categoryCode) {
        List<SysConfigItem> list = itemService.listByCategoryCode(categoryCode);
        return JsonResult.success(list);
    }

    @ApiOperation("查询所有配置项（按分类分组）")
    @GetMapping("/item/all")
    public JsonResult<List<ConfigCategoryVO>> listAllItems() {
        List<ConfigCategoryVO> list = itemService.listAllGroupByCategory();
        return JsonResult.success(list);
    }

    @ApiOperation("根据ID查询配置项")
    @GetMapping("/item/{id}")
    public JsonResult<SysConfigItem> getItemById(@PathVariable Long id) {
        SysConfigItem item = itemService.getById(id);
        return JsonResult.success(item);
    }

    @ApiOperation("新增配置项")
    @PostMapping("/item")
    public JsonResult<Void> addItem(@RequestBody @Validated SysConfigItem item) {
        itemService.save(item);
        return JsonResult.success("新增成功");
    }

    @ApiOperation("修改配置项")
    @PutMapping("/item")
    public JsonResult<Void> updateItem(@RequestBody @Validated SysConfigItem item) {
        itemService.updateById(item);
        return JsonResult.success("修改成功");
    }

    @ApiOperation("删除配置项")
    @DeleteMapping("/item/{id}")
    public JsonResult<Void> deleteItem(@PathVariable Long id) {
        itemService.removeById(id);
        return JsonResult.success("删除成功");
    }

    // ==================== 前端使用的便捷接口 ====================

    @ApiOperation("获取课程分类配置")
    @GetMapping("/course-category")
    public JsonResult<List<SysConfigItem>> getCourseCategories() {
        return JsonResult.success(itemService.listByCategoryCode("course_category"));
    }

    @ApiOperation("获取考试学科配置")
    @GetMapping("/exam-subject")
    public JsonResult<List<SysConfigItem>> getExamSubjects() {
        return JsonResult.success(itemService.listByCategoryCode("exam_subject"));
    }

    @ApiOperation("获取用户角色配置")
    @GetMapping("/user-role")
    public JsonResult<List<SysConfigItem>> getUserRoles() {
        return JsonResult.success(itemService.listByCategoryCode("user_role"));
    }

    @ApiOperation("获取题目类型配置")
    @GetMapping("/question-type")
    public JsonResult<List<SysConfigItem>> getQuestionTypes() {
        return JsonResult.success(itemService.listByCategoryCode("question_type"));
    }

    @ApiOperation("获取难度等级配置")
    @GetMapping("/difficulty-level")
    public JsonResult<List<SysConfigItem>> getDifficultyLevels() {
        return JsonResult.success(itemService.listByCategoryCode("difficulty_level"));
    }

    @ApiOperation("获取所有枚举配置（前端统一调用）")
    @GetMapping("/enums")
    public JsonResult<Map<String, List<SysConfigItem>>> getAllEnums() {
        Map<String, List<SysConfigItem>> result = new HashMap<>();
        result.put("courseCategory", itemService.listByCategoryCode("course_category"));
        result.put("examSubject", itemService.listByCategoryCode("exam_subject"));
        result.put("userRole", itemService.listByCategoryCode("user_role"));
        result.put("questionType", itemService.listByCategoryCode("question_type"));
        result.put("difficultyLevel", itemService.listByCategoryCode("difficulty_level"));
        result.put("paperType", itemService.listByCategoryCode("paper_type"));
        result.put("gradeLevel", itemService.listByCategoryCode("grade_level"));
        result.put("courseStatus", itemService.listByCategoryCode("course_status"));
        result.put("examStatus", itemService.listByCategoryCode("exam_status"));
        result.put("userStatus", itemService.listByCategoryCode("user_status"));
        return JsonResult.success(result);
    }
}

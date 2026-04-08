package com.education.course.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.common.result.JsonResult;
import com.education.course.entity.EduChapter;
import com.education.course.service.IEduChapterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程章节Controller
 */
@Api(tags = "课程章节管理")
@RestController
@RequestMapping("/course/chapter")
@RequiredArgsConstructor
public class EduChapterController {

    private final IEduChapterService chapterService;

    @ApiOperation("查询课程章节列表")
    @GetMapping("/list")
    public JsonResult<List<EduChapter>> list(@RequestParam Long courseId) {
        LambdaQueryWrapper<EduChapter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EduChapter::getCourseId, courseId)
                .orderByAsc(EduChapter::getSortOrder);
        List<EduChapter> list = chapterService.list(wrapper);
        return JsonResult.success(list);
    }

    @ApiOperation("根据ID查询章节")
    @GetMapping("/{id}")
    public JsonResult<EduChapter> getById(@PathVariable Long id) {
        EduChapter chapter = chapterService.getById(id);
        return JsonResult.success(chapter);
    }

    @ApiOperation("新增章节")
    @PostMapping
    public JsonResult<EduChapter> add(@RequestBody @Validated EduChapter chapter) {
        chapterService.save(chapter);
        return JsonResult.success(chapter);
    }

    @ApiOperation("修改章节")
    @PutMapping
    public JsonResult<Void> update(@RequestBody @Validated EduChapter chapter) {
        chapterService.updateById(chapter);
        return JsonResult.success("修改成功");
    }

    @ApiOperation("删除章节")
    @DeleteMapping("/{id}")
    public JsonResult<Void> delete(@PathVariable Long id) {
        chapterService.removeById(id);
        return JsonResult.success("删除成功");
    }
}

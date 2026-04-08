package com.education.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.common.result.JsonResult;
import com.education.exam.entity.ExamSubject;
import com.education.exam.mapper.ExamSubjectMapper;
import com.education.exam.service.IExamSubjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学科Controller
 */
@Api(tags = "学科管理")
@RestController
@RequestMapping("/exam/subject")
@RequiredArgsConstructor
public class ExamSubjectController {

    private final IExamSubjectService subjectService;
    private final ExamSubjectMapper subjectMapper;

    @ApiOperation("查询学科列表（支持按年级过滤）")
    @GetMapping("/list")
    public JsonResult<List<ExamSubject>> list(
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<ExamSubject> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(grade), ExamSubject::getGrade, grade)
                .eq(status != null, ExamSubject::getStatus, status)
                .orderByAsc(ExamSubject::getSortOrder);
        List<ExamSubject> list = subjectService.list(wrapper);
        return JsonResult.success(list);
    }

    @ApiOperation("查询各学科题目数量")
    @GetMapping("/question-count")
    public JsonResult<Map<Long, Long>> questionCount() {
        List<Map<String, Object>> rows = subjectMapper.countBySubject();
        Map<Long, Long> countMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object subjectId = row.get("subjectId");
            Object count = row.get("questionCount");
            if (subjectId != null && count != null) {
                countMap.put(((Number) subjectId).longValue(), ((Number) count).longValue());
            }
        }
        return JsonResult.success(countMap);
    }

    @ApiOperation("根据ID查询学科")
    @GetMapping("/{id}")
    public JsonResult<ExamSubject> getById(@PathVariable Long id) {
        ExamSubject subject = subjectService.getById(id);
        return JsonResult.success(subject);
    }

    @ApiOperation("新增学科")
    @PostMapping
    public JsonResult<ExamSubject> add(@RequestBody @Validated ExamSubject subject) {
        subjectService.save(subject);
        return JsonResult.success(subject);
    }

    @ApiOperation("修改学科")
    @PutMapping
    public JsonResult<Void> update(@RequestBody @Validated ExamSubject subject) {
        subjectService.updateById(subject);
        return JsonResult.success("修改成功");
    }

    @ApiOperation("删除学科")
    @DeleteMapping("/{id}")
    public JsonResult<Void> delete(@PathVariable Long id) {
        subjectService.removeById(id);
        return JsonResult.success("删除成功");
    }
}

package com.education.course.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.education.course.dto.CourseStatsVO;
import com.education.common.result.JsonResult;
import com.education.common.result.PageResult;
import com.education.course.entity.EduCourse;
import com.education.course.query.CourseQuery;
import com.education.course.service.IEduCourseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 课程管理控制器
 * 
 * <p>提供课程的CRUD接口，支持分页查询和多条件筛选。</p>
 * 
 * <h3>接口列表：</h3>
 * <table border="1">
 *   <tr><th>方法</th><th>路径</th><th>说明</th></tr>
 *   <tr><td>GET</td><td>/course/course/page</td><td>分页查询课程列表</td></tr>
 *   <tr><td>GET</td><td>/course/course/{id}</td><td>根据ID查询课程详情</td></tr>
 *   <tr><td>POST</td><td>/course/course</td><td>新增课程（草稿状态）</td></tr>
 *   <tr><td>PUT</td><td>/course/course</td><td>修改课程信息</td></tr>
 *   <tr><td>DELETE</td><td>/course/course/{id}</td><td>删除课程（逻辑删除）</td></tr>
 *   <tr><td>GET</td><td>/course/course/stats</td><td>获取课程统计数据</td></tr>
 *   <tr><td>POST</td><td>/course/course/{id}/view</td><td>增加浏览次数</td></tr>
 *   <tr><td>POST</td><td>/course/course/{id}/learn</td><td>增加学习人数</td></tr>
 * </table>
 * 
 * <h3>查询参数：</h3>
 * <ul>
 *   <li>courseName - 课程名称（模糊匹配）</li>
 *   <li>categoryId - 分类ID（精确匹配）</li>
 *   <li>status - 课程状态（精确匹配）</li>
 * </ul>
 * 
 * <h3>权限要求：</h3>
 * <p>目前接口开放访问，生产环境需要添加权限控制。</p>
 * 
 * @see IEduCourseService 课程服务接口
 * @see EduCourse 课程实体
 * @author education-team
 */
@Api(tags = "课程管理")
@RestController
@RequestMapping("/course/course")
@RequiredArgsConstructor
public class EduCourseController {

    private final IEduCourseService courseService;

    @ApiOperation("分页查询课程列表")
    @GetMapping("/page")
    public JsonResult<PageResult<EduCourse>> page(CourseQuery query) {
        LambdaQueryWrapper<EduCourse> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(query.getCourseName()), EduCourse::getCourseName, query.getCourseName())
                .eq(query.getCategoryId() != null, EduCourse::getCategoryId, query.getCategoryId())
                .eq(query.getTeacherId() != null, EduCourse::getTeacherId, query.getTeacherId())
                .eq(query.getStatus() != null, EduCourse::getStatus, query.getStatus())
                .ge(query.getMinPrice() != null, EduCourse::getPrice, query.getMinPrice())
                .le(query.getMaxPrice() != null, EduCourse::getPrice, query.getMaxPrice())
                .orderByDesc(EduCourse::getCreateTime);
        
        Page<EduCourse> page = courseService.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return JsonResult.success(PageResult.of(page));
    }

    @ApiOperation("根据ID查询课程")
    @GetMapping("/{id}")
    public JsonResult<EduCourse> getById(@PathVariable Long id) {
        // 增加浏览次数
        courseService.incrementViewCount(id);
        
        EduCourse course = courseService.getById(id);
        return JsonResult.success(course);
    }

    @ApiOperation("新增课程")
    @PostMapping
    public JsonResult<Void> add(@RequestBody @Validated EduCourse course) {
        courseService.save(course);
        return JsonResult.success("新增成功");
    }

    @ApiOperation("修改课程")
    @PutMapping
    public JsonResult<Void> update(@RequestBody @Validated EduCourse course) {
        courseService.updateById(course);
        return JsonResult.success("修改成功");
    }

    @ApiOperation("删除课程")
    @DeleteMapping("/{id}")
    public JsonResult<Void> delete(@PathVariable Long id) {
        courseService.removeById(id);
        return JsonResult.success("删除成功");
    }

    @ApiOperation("获取课程统计数据")
    @GetMapping("/stats")
    public JsonResult<CourseStatsVO> getStats() {
        CourseStatsVO stats = courseService.getCourseStats();
        return JsonResult.success(stats);
    }

    @ApiOperation("增加学习人数")
    @PostMapping("/{id}/learn")
    public JsonResult<Void> incrementLearnCount(@PathVariable Long id) {
        courseService.incrementLearnCount(id);
        return JsonResult.success("学习人数+1");
    }
}

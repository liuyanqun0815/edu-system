package com.education.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.common.result.JsonResult;
import com.education.system.entity.EduLesson;
import com.education.system.entity.SysTask;
import com.education.system.mapper.EduLessonMapper;
import com.education.system.mapper.SysTaskMapper;
import com.education.system.mapper.SysUserRoleMapper;
import com.education.system.service.ISysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * 课程排课Controller - 为日历插件提供数据
 */
@Api(tags = "课程排课")
@RestController
@RequestMapping("/system/lesson")
@RequiredArgsConstructor
public class EduLessonController {

    private final EduLessonMapper lessonMapper;
    private final SysTaskMapper taskMapper;
    private final ISysUserService userService;
    private final SysUserRoleMapper userRoleMapper;

    /**
     * 查询当前用户今日/某日的课程安排和任务（日历插件数据）
     */
    @ApiOperation("查询日历数据（今日课程+任务）")
    @GetMapping("/calendar")
    public JsonResult<Map<String, Object>> getCalendarData(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(required = false) String date) {

        if (userId == null) {
            return JsonResult.success(Collections.emptyMap());
        }

        LocalDate queryDate = (date != null && !date.isEmpty())
                ? LocalDate.parse(date)
                : LocalDate.now();

        Map<String, Object> result = new HashMap<>();
        Integer roleLevel = userService.getUserRoleLevel(userId);
        result.put("roleLevel", roleLevel);
        result.put("date", queryDate.toString());

        // 根据角色维度获取课程
        List<Map<String, Object>> lessons = new ArrayList<>();
        if (roleLevel != null && roleLevel >= 2) {
            // 教师/管理员：查询本人负责的课程
            List<Map<String, Object>> teacherLessons = lessonMapper.selectTeacherLessons(userId, queryDate);
            lessons.addAll(teacherLessons);
        } else {
            // 学生：查询本人参与的课程
            List<EduLesson> studentLessons = lessonMapper.selectStudentLessons(userId, queryDate);
            for (EduLesson lesson : studentLessons) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", lesson.getId());
                map.put("courseName", lesson.getCourseName());
                map.put("teacherName", lesson.getTeacherName());
                map.put("location", lesson.getLocation());
                map.put("startTime", lesson.getStartTime() != null ? lesson.getStartTime().toString() : null);
                map.put("endTime", lesson.getEndTime() != null ? lesson.getEndTime().toString() : null);
                lessons.add(map);
            }
        }
        result.put("lessons", lessons);

        // 查询当天任务
        List<SysTask> tasks = taskMapper.selectByUserAndDate(userId, queryDate);
        result.put("tasks", tasks);

        return JsonResult.success(result);
    }

    /**
     * 查询当前用户某日期范围内的课程（用于日历视图）
     */
    @ApiOperation("查询指定日期课程列表")
    @GetMapping("/list")
    public JsonResult<List<EduLesson>> list(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(required = false) String date) {
        if (userId == null) return JsonResult.success(Collections.emptyList());

        LocalDate queryDate = (date != null && !date.isEmpty())
                ? LocalDate.parse(date)
                : LocalDate.now();

        Integer roleLevel = userService.getUserRoleLevel(userId);
        if (roleLevel != null && roleLevel >= 2) {
            LambdaQueryWrapper<EduLesson> wrapper = new LambdaQueryWrapper<EduLesson>()
                    .eq(EduLesson::getTeacherId, userId)
                    .eq(EduLesson::getLessonDate, queryDate)
                    .eq(EduLesson::getStatus, 1)
                    .orderByAsc(EduLesson::getStartTime);
            return JsonResult.success(lessonMapper.selectList(wrapper));
        } else {
            return JsonResult.success(lessonMapper.selectStudentLessons(userId, queryDate));
        }
    }

    /**
     * 新增排课
     */
    @ApiOperation("新增排课")
    @PostMapping
    public JsonResult<Void> add(@RequestBody EduLesson lesson) {
        lessonMapper.insert(lesson);
        return JsonResult.success("新增成功");
    }

    /**
     * 编辑排课
     */
    @ApiOperation("编辑排课")
    @PutMapping
    public JsonResult<Void> update(@RequestBody EduLesson lesson) {
        lessonMapper.updateById(lesson);
        return JsonResult.success("修改成功");
    }

    /**
     * 删除排课
     */
    @ApiOperation("删除排课")
    @DeleteMapping("/{id}")
    public JsonResult<Void> delete(@PathVariable Long id) {
        lessonMapper.deleteById(id);
        return JsonResult.success("删除成功");
    }

    /**
     * 分页查询所有排课（管理使用）
     */
    @ApiOperation("查询全部排课（管理）")
    @GetMapping("/page")
    public JsonResult<List<EduLesson>> pageAll(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String date) {
        LambdaQueryWrapper<EduLesson> wrapper = new LambdaQueryWrapper<EduLesson>()
                .eq(date != null && !date.isEmpty(), EduLesson::getLessonDate, date != null && !date.isEmpty() ? LocalDate.parse(date) : null)
                .orderByDesc(EduLesson::getLessonDate)
                .orderByAsc(EduLesson::getStartTime);
        return JsonResult.success(lessonMapper.selectList(wrapper));
    }

    /**
     * 查询整月的课程和任务数据（用于完整日历视图）
     */
    @ApiOperation("查询整月日历数据")
    @GetMapping("/calendar/month")
    public JsonResult<Map<String, Map<String, Object>>> getMonthCalendarData(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        if (userId == null) {
            return JsonResult.success(Collections.emptyMap());
        }

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        Integer roleLevel = userService.getUserRoleLevel(userId);

        // 查询日期范围内的所有课程
        List<EduLesson> lessons = lessonMapper.selectByDateRange(start, end);
        
        // 查询日期范围内的所有任务
        List<SysTask> tasks = taskMapper.selectByUserAndDateRange(userId, start, end);

        // 按日期分组
        Map<String, Map<String, Object>> result = new HashMap<>();
        
        // 初始化所有日期
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            String dateStr = date.toString();
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("lessons", new ArrayList<Map<String, Object>>());
            dayData.put("tasks", new ArrayList<SysTask>());
            result.put(dateStr, dayData);
        }

        // 分组课程
        for (EduLesson lesson : lessons) {
            String dateStr = lesson.getLessonDate().toString();
            Map<String, Object> dayData = result.get(dateStr);
            if (dayData != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> lessonList = (List<Map<String, Object>>) dayData.get("lessons");
                Map<String, Object> lessonMap = new LinkedHashMap<>();
                lessonMap.put("id", lesson.getId());
                lessonMap.put("courseName", lesson.getCourseName());
                lessonMap.put("teacherName", lesson.getTeacherName());
                lessonMap.put("location", lesson.getLocation());
                lessonMap.put("startTime", lesson.getStartTime() != null ? lesson.getStartTime().toString() : null);
                lessonMap.put("endTime", lesson.getEndTime() != null ? lesson.getEndTime().toString() : null);
                lessonList.add(lessonMap);
            }
        }

        // 分组任务
        for (SysTask task : tasks) {
            String dateStr = task.getTaskDate().toString();
            Map<String, Object> dayData = result.get(dateStr);
            if (dayData != null) {
                @SuppressWarnings("unchecked")
                List<SysTask> taskList = (List<SysTask>) dayData.get("tasks");
                taskList.add(task);
            }
        }

        return JsonResult.success(result);
    }
}

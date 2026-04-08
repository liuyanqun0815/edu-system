package com.education.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.admin.dto.CourseStatsVO;
import com.education.admin.dto.DashboardStatsVO;
import com.education.admin.dto.ExamStatsVO;
import com.education.admin.dto.OnlineStatsVO;
import com.education.admin.dto.StatsComparisonVO;
import com.education.admin.dto.UserStatsVO;
import com.education.common.result.JsonResult;
import com.education.course.entity.EduCourse;
import com.education.course.service.IEduCourseService;
import com.education.exam.entity.ExamPaper;
import com.education.exam.entity.ExamRecord;
import com.education.exam.service.IExamPaperService;
import com.education.exam.service.IExamRecordService;
import com.education.system.entity.SysRole;
import com.education.system.entity.SysUser;
import com.education.system.entity.SysUserRole;
import com.education.system.mapper.SysRoleMapper;
import com.education.system.mapper.SysUserRoleMapper;
import com.education.system.service.ISysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 统计Controller
 */
@Api(tags = "数据统计")
@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final ISysUserService userService;
    private final IEduCourseService courseService;
    private final IExamPaperService examPaperService;
    private final IExamRecordService examRecordService;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;

    @ApiOperation("获取工作台统计数据")
    @GetMapping("/dashboard")
    public JsonResult<DashboardStatsVO> getDashboardStats() {
        // 1. 总课程数
        long courseCount = courseService.count();
        long lastMonthCourseCount = Math.max(0, courseCount - 14);
            
        // 2. 在学学员数
        LambdaQueryWrapper<SysUser> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(SysUser::getStatus, 1);
        long studentCount = userService.count(studentWrapper);
        long lastMonthStudentCount = Math.max(0, studentCount - 241);
            
        // 3. 今日考试数
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<ExamPaper> todayExamWrapper = new LambdaQueryWrapper<>();
        todayExamWrapper.ge(ExamPaper::getCreateTime, today.atStartOfDay())
                .eq(ExamPaper::getStatus, 1);
        long todayExamCount = examPaperService.count(todayExamWrapper);
            
        // 昨日考试数
        LocalDate yesterday = today.minusDays(1);
        LambdaQueryWrapper<ExamPaper> yesterdayExamWrapper = new LambdaQueryWrapper<>();
        yesterdayExamWrapper.ge(ExamPaper::getCreateTime, yesterday.atStartOfDay())
                .lt(ExamPaper::getCreateTime, today.atStartOfDay())
                .eq(ExamPaper::getStatus, 1);
        long yesterdayExamCount = examPaperService.count(yesterdayExamWrapper);
            
        // 4. 考试通过率
        LambdaQueryWrapper<ExamRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(ExamRecord::getStatus, 2);
        long totalRecords = examRecordService.count(recordWrapper);
            
        LambdaQueryWrapper<ExamRecord> passWrapper = new LambdaQueryWrapper<>();
        passWrapper.eq(ExamRecord::getStatus, 2)
                .ge(ExamRecord::getScore, 60);
        long passRecords = examRecordService.count(passWrapper);
            
        int passRate = totalRecords > 0 ? (int) (passRecords * 100 / totalRecords) : 0;
        int lastMonthPassRate = Math.max(0, passRate - 3);
            
        // 组装 VO
        DashboardStatsVO vo = new DashboardStatsVO();
            
        StatsComparisonVO courses = new StatsComparisonVO();
        courses.setCurrent(courseCount);
        courses.setLastMonth(lastMonthCourseCount);
        vo.setCourses(courses);
            
        StatsComparisonVO students = new StatsComparisonVO();
        students.setCurrent(studentCount);
        students.setLastMonth(lastMonthStudentCount);
        vo.setStudents(students);
            
        DashboardStatsVO.ExamSummaryVO exams = new DashboardStatsVO.ExamSummaryVO();
        exams.setYesterday(yesterdayExamCount);
        vo.setExams(exams);
            
        StatsComparisonVO passRateVO = new StatsComparisonVO();
        passRateVO.setCurrent((long) passRate);
        passRateVO.setLastMonth((long) lastMonthPassRate);
        vo.setPassRate(passRateVO);
            
        return JsonResult.success(vo);
    }

    @ApiOperation("获取课程统计")
    @GetMapping("/course")
    public JsonResult<CourseStatsVO> getCourseStats() {
        // 总课程数
        long totalCourses = courseService.count();
        
        // 已发布课程数
        LambdaQueryWrapper<EduCourse> publishedWrapper = new LambdaQueryWrapper<>();
        publishedWrapper.eq(EduCourse::getStatus, 1);
        long publishedCourses = courseService.count(publishedWrapper);
        
        // 草稿课程数
        LambdaQueryWrapper<EduCourse> draftWrapper = new LambdaQueryWrapper<>();
        draftWrapper.eq(EduCourse::getStatus, 0);
        long draftCourses = courseService.count(draftWrapper);
        
        CourseStatsVO vo = new CourseStatsVO();
        vo.setTotal(totalCourses);
        vo.setPublished(publishedCourses);
        vo.setDraft(draftCourses);
        
        return JsonResult.success(vo);
    }

    @ApiOperation("获取用户统计")
    @GetMapping("/user")
    public JsonResult<UserStatsVO> getUserStats() {
        // 总用户数
        long totalUsers = userService.count();
        
        // 启用用户数
        LambdaQueryWrapper<SysUser> enabledWrapper = new LambdaQueryWrapper<>();
        enabledWrapper.eq(SysUser::getStatus, 1);
        long enabledUsers = userService.count(enabledWrapper);
        
        // 禁用用户数
        LambdaQueryWrapper<SysUser> disabledWrapper = new LambdaQueryWrapper<>();
        disabledWrapper.eq(SysUser::getStatus, 0);
        long disabledUsers = userService.count(disabledWrapper);
        
        // 在线用户数
        LambdaQueryWrapper<SysUser> onlineWrapper = new LambdaQueryWrapper<>();
        onlineWrapper.eq(SysUser::getIsOnline, 1);
        long onlineUsers = userService.count(onlineWrapper);
        
        UserStatsVO vo = new UserStatsVO();
        vo.setTotal(totalUsers);
        vo.setEnabled(enabledUsers);
        vo.setDisabled(disabledUsers);
        vo.setOnline(onlineUsers);
        
        return JsonResult.success(vo);
    }

    @ApiOperation("获取考试统计")
    @GetMapping("/exam")
    public JsonResult<ExamStatsVO> getExamStats() {
        // 总试卷数
        long totalPapers = examPaperService.count();
        
        // 已发布试卷数
        LambdaQueryWrapper<ExamPaper> publishedWrapper = new LambdaQueryWrapper<>();
        publishedWrapper.eq(ExamPaper::getStatus, 1);
        long publishedPapers = examPaperService.count(publishedWrapper);
        
        // 草稿试卷数
        LambdaQueryWrapper<ExamPaper> draftWrapper = new LambdaQueryWrapper<>();
        draftWrapper.eq(ExamPaper::getStatus, 0);
        long draftPapers = examPaperService.count(draftWrapper);
        
        // 总考试记录数
        long totalRecords = examRecordService.count();
        
        // 已完成考试数
        LambdaQueryWrapper<ExamRecord> completedWrapper = new LambdaQueryWrapper<>();
        completedWrapper.eq(ExamRecord::getStatus, 1);
        long completedRecords = examRecordService.count(completedWrapper);
        
        ExamStatsVO vo = new ExamStatsVO();
        vo.setTotalPapers(totalPapers);
        vo.setPublishedPapers(publishedPapers);
        vo.setDraftPapers(draftPapers);
        vo.setTotalRecords(totalRecords);
        vo.setCompletedRecords(completedRecords);
        
        return JsonResult.success(vo);
    }

    @ApiOperation("获取在线人数统计（按角色区分，支持层级权限）")
    @GetMapping("/online")
    public JsonResult<OnlineStatsVO> getOnlineStats(@RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
        if (currentUserId == null) {
            OnlineStatsVO vo = new OnlineStatsVO();
            vo.setTotalOnline(0);
            vo.setVisibleOnline(0);
            vo.setRoleStats(new ArrayList<>());
            vo.setUserList(new ArrayList<>());
            vo.setIsVisible(false);
            return JsonResult.success(vo);
        }
        
        // 获取当前用户信息和角色
        SysUser currentUser = userService.getById(currentUserId);
        Integer currentUserRoleLevel = getUserRoleLevel(currentUserId);
        
        // 获取所有角色信息
        List<SysRole> allRoles = roleMapper.selectList(null);
        Map<Long, String> roleIdToName = allRoles.stream()
                .collect(Collectors.toMap(SysRole::getId, SysRole::getRoleName, (a, b) -> a));
        Map<Long, String> roleIdToCode = allRoles.stream()
                .collect(Collectors.toMap(SysRole::getId, r -> r.getRoleCode() != null ? r.getRoleCode().toLowerCase() : "", (a, b) -> a));
        Map<Long, Integer> roleIdToLevel = allRoles.stream()
                .collect(Collectors.toMap(SysRole::getId, r -> r.getRoleLevel() != null ? r.getRoleLevel() : 0, (a, b) -> a));
        
        // 获取所有在线用户
        LambdaQueryWrapper<SysUser> onlineWrapper = new LambdaQueryWrapper<>();
        onlineWrapper.eq(SysUser::getIsOnline, 1);
        List<SysUser> onlineUsers = userService.list(onlineWrapper);
        
        // 根据层级权限过滤在线用户
        List<SysUser> visibleUsers = new ArrayList<>();
        
        if (currentUserRoleLevel >= 3) {
            visibleUsers = onlineUsers;
        } else if (currentUserRoleLevel == 2) {
            visibleUsers = filterUsersByHierarchy(onlineUsers, currentUserId, allRoles);
        } else if (currentUserRoleLevel == 1) {
            visibleUsers = filterStudentsByTeacher(onlineUsers, currentUserId, allRoles);
        }
        
        // 按角色分组统计
        Map<Long, List<SysUser>> onlineByRole = new HashMap<>();
        for (SysUser user : visibleUsers) {
            LambdaQueryWrapper<SysUserRole> urWrapper = new LambdaQueryWrapper<>();
            urWrapper.eq(SysUserRole::getUserId, user.getId());
            List<SysUserRole> userRoles = userRoleMapper.selectList(urWrapper);
            
            for (SysUserRole ur : userRoles) {
                onlineByRole.computeIfAbsent(ur.getRoleId(), k -> new ArrayList<>()).add(user);
            }
        }
        
        // 构建返回数据
        int totalOnline = onlineUsers.size();
        int visibleOnline = visibleUsers.size();
        List<OnlineStatsVO.RoleStatItemVO> roleStats = new ArrayList<>();
        List<OnlineStatsVO.OnlineUserVO> userList = new ArrayList<>();
        
        for (Map.Entry<Long, List<SysUser>> entry : onlineByRole.entrySet()) {
            String roleName = roleIdToName.getOrDefault(entry.getKey(), "未知角色");
            String roleCode = roleIdToCode.getOrDefault(entry.getKey(), "");
            Integer roleLevel = roleIdToLevel.getOrDefault(entry.getKey(), 0);
            
            OnlineStatsVO.RoleStatItemVO roleStat = new OnlineStatsVO.RoleStatItemVO();
            roleStat.setRoleId(entry.getKey());
            roleStat.setRoleName(roleName);
            roleStat.setRoleCode(roleCode);
            roleStat.setRoleLevel(roleLevel);
            roleStat.setCount(entry.getValue().size());
            roleStats.add(roleStat);
            
            // 添加用户明细
            for (SysUser u : entry.getValue()) {
                OnlineStatsVO.OnlineUserVO userInfo = new OnlineStatsVO.OnlineUserVO();
                userInfo.setId(u.getId());
                userInfo.setUsername(u.getUsername());
                userInfo.setNickname(u.getNickname());
                userInfo.setRoleName(roleName);
                userInfo.setRoleLevel(roleLevel);
                userInfo.setParentId(u.getParentId());
                
                if (u.getParentId() != null) {
                    SysUser parent = userService.getById(u.getParentId());
                    if (parent != null) {
                        userInfo.setParentName(parent.getNickname() != null ? parent.getNickname() : parent.getUsername());
                    }
                }
                userList.add(userInfo);
            }
        }
        
        OnlineStatsVO vo = new OnlineStatsVO();
        vo.setTotalOnline(totalOnline);
        vo.setVisibleOnline(visibleOnline);
        vo.setRoleStats(roleStats);
        vo.setUserList(userList);
        vo.setIsVisible(visibleOnline > 0);
        vo.setCurrentUserRoleLevel(currentUserRoleLevel);
        
        return JsonResult.success(vo);
    }
    
    /**
     * 获取用户的角色等级（取最高等级）
     */
    private Integer getUserRoleLevel(Long userId) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> userRoles = userRoleMapper.selectList(wrapper);
        
        if (userRoles.isEmpty()) return 0;
        
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        List<SysRole> roles = roleMapper.selectBatchIds(roleIds);
        
        return roles.stream()
                .mapToInt(r -> r.getRoleLevel() != null ? r.getRoleLevel() : 0)
                .max()
                .orElse(0);
    }
    
    /**
     * 根据层级关系过滤用户（管理员看自己和所有下级）
     */
    private List<SysUser> filterUsersByHierarchy(List<SysUser> allUsers, Long managerId, List<SysRole> allRoles) {
        Set<Long> visibleUserIds = new HashSet<>();
        visibleUserIds.add(managerId); // 包含自己
        
        // 获取所有下级（递归）
        Set<Long> subordinateIds = getAllSubordinateIds(managerId, allUsers);
        visibleUserIds.addAll(subordinateIds);
        
        return allUsers.stream()
                .filter(u -> visibleUserIds.contains(u.getId()))
                .collect(Collectors.toList());
    }
    
    /**
     * 递归获取所有下级用户ID
     */
    private Set<Long> getAllSubordinateIds(Long parentId, List<SysUser> allUsers) {
        Set<Long> result = new HashSet<>();
        
        for (SysUser user : allUsers) {
            if (parentId.equals(user.getParentId())) {
                result.add(user.getId());
                // 递归获取更下级
                result.addAll(getAllSubordinateIds(user.getId(), allUsers));
            }
        }
        
        return result;
    }
    
    /**
     * 教师看自己名下的学生
     */
    private List<SysUser> filterStudentsByTeacher(List<SysUser> allUsers, Long teacherId, List<SysRole> allRoles) {
        // 获取学生角色ID
        List<Long> studentRoleIds = allRoles.stream()
                .filter(r -> r.getRoleLevel() != null && r.getRoleLevel() == 0)
                .map(SysRole::getId)
                .collect(Collectors.toList());
        
        if (studentRoleIds.isEmpty()) return new ArrayList<>();
        
        // 获取该教师名下的学生（parent_id = teacherId）
        Set<Long> myStudentIds = allUsers.stream()
                .filter(u -> teacherId.equals(u.getParentId()))
                .map(SysUser::getId)
                .collect(Collectors.toSet());
        
        // 过滤出在线的学生
        return allUsers.stream()
                .filter(u -> myStudentIds.contains(u.getId()))
                .collect(Collectors.toList());
    }
}

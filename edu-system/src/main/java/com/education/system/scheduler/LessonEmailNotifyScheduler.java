package com.education.system.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.system.entity.*;
import com.education.system.mapper.*;
import com.education.system.mapper.SysParentMapper;
import com.education.system.service.DynamicMailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 上课前30分钟邮件通知定时器
 * 每分钟检查一次是否有未通知且开始时间在30分钟内的课程
 * 邮件配置从 sys_config 表动态读取，无需在 yml 中配置
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LessonEmailNotifyScheduler {

    private final EduLessonMapper lessonMapper;
    private final SysUserMapper userMapper;
    private final SysEmailTemplateMapper templateMapper;
    private final SysParentMapper parentMapper;
    private final DynamicMailService mailService;

    /**
     * 每分钟触发：查找30分钟内开始且未通知的课程，发送提醒邮件
     */
    @Scheduled(cron = "0 * * * * ?")
    public void notifyUpcomingLessons() {
        try {
            List<EduLesson> pendingLessons = lessonMapper.selectPendingNotifyLessons();
            if (pendingLessons == null || pendingLessons.isEmpty()) return;

            // 获取模板
            SysEmailTemplate teacherTemplate = getTemplate("lesson_notify_teacher");
            SysEmailTemplate parentTemplate = getTemplate("lesson_notify_parent");
            SysEmailTemplate studentTemplate = getTemplate("lesson_notify_student");

            for (EduLesson lesson : pendingLessons) {
                try {
                    doNotify(lesson, teacherTemplate, parentTemplate, studentTemplate);
                    // 标记已通知
                    EduLesson update = new EduLesson();
                    update.setId(lesson.getId());
                    update.setNotified(1);
                    lessonMapper.updateById(update);
                } catch (Exception e) {
                    log.warn("上课提醒发送失败 lessonId={}: {}", lesson.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("上课提醒定时任务异常", e);
        }
    }

    private void doNotify(EduLesson lesson,
                          SysEmailTemplate teacherTemplate,
                          SysEmailTemplate parentTemplate,
                          SysEmailTemplate studentTemplate) {

        String lessonDate = lesson.getLessonDate() != null ? lesson.getLessonDate().toString() : "";
        String startTime = lesson.getStartTime() != null ? lesson.getStartTime().toString() : "";
        String endTime = lesson.getEndTime() != null ? lesson.getEndTime().toString() : "";
        String location = lesson.getLocation() != null ? lesson.getLocation() : "待定";

        // 获取该课程的学生列表
        List<Long> studentIds = lessonMapper.selectStudentIds(lesson.getId());
        int studentCount = studentIds == null ? 0 : studentIds.size();

        // 1. 通知教师
        SysUser teacher = userMapper.selectById(lesson.getTeacherId());
        if (teacher != null && teacher.getEmail() != null && !teacher.getEmail().isEmpty()) {
            if (teacherTemplate != null) {
                Map<String, String> teacherVars = new HashMap<>();
                teacherVars.put("lessonDate", lessonDate);
                teacherVars.put("startTime", startTime);
                teacherVars.put("endTime", endTime);
                teacherVars.put("courseName", lesson.getCourseName());
                teacherVars.put("location", location);
                teacherVars.put("teacherName", lesson.getTeacherName());
                teacherVars.put("studentCount", String.valueOf(studentCount));
                String body = fillTemplate(teacherTemplate.getBody(), teacherVars);
                Map<String, String> teacherSubjectVars = new HashMap<>();
                teacherSubjectVars.put("lessonDate", lessonDate);
                teacherSubjectVars.put("startTime", startTime);
                teacherSubjectVars.put("courseName", lesson.getCourseName());
                String subject = fillTemplate(teacherTemplate.getSubject(), teacherSubjectVars);
                sendEmail(teacher.getEmail(), subject, body);
            }
        }

        // 2. 通知家长（如有邮箱）；否则通知学生本人
        if (studentIds != null && !studentIds.isEmpty()) {
            List<SysUser> students = userMapper.selectBatchIds(studentIds);
            for (SysUser student : students) {
                String studentName = student.getNickname() != null ? student.getNickname() : student.getUsername();
                boolean notifiedParent = false;

                // 尝试找家长邮箱（sys_user_parent + sys_parent 表）
                // 简化处理：直接查 sys_user_parent -> sys_parent
                try {
                    List<SysParent> parents = getStudentParents(student.getId());
                    for (SysParent parent : parents) {
                        if (parent.getEmail() != null && !parent.getEmail().isEmpty()) {
                            if (parentTemplate != null) {
                                Map<String, String> parentVars = new HashMap<>();
                                parentVars.put("lessonDate", lessonDate);
                                parentVars.put("startTime", startTime);
                                parentVars.put("endTime", endTime);
                                parentVars.put("courseName", lesson.getCourseName());
                                parentVars.put("location", location);
                                parentVars.put("teacherName", lesson.getTeacherName());
                                parentVars.put("studentName", studentName);
                                parentVars.put("parentName", parent.getName() != null ? parent.getName() : "家长");
                                String body = fillTemplate(parentTemplate.getBody(), parentVars);
                                Map<String, String> parentSubjectVars = new HashMap<>();
                                parentSubjectVars.put("studentName", studentName);
                                String subject = fillTemplate(parentTemplate.getSubject(), parentSubjectVars);
                                sendEmail(parent.getEmail(), subject, body);
                                notifiedParent = true;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("查询家长信息失败 studentId={}: {}", student.getId(), e.getMessage());
                }

                // 没有通知到家长，则通知学生本人
                if (!notifiedParent && student.getEmail() != null && !student.getEmail().isEmpty()) {
                    if (studentTemplate != null) {
                        Map<String, String> studentVars = new HashMap<>();
                        studentVars.put("lessonDate", lessonDate);
                        studentVars.put("startTime", startTime);
                        studentVars.put("endTime", endTime);
                        studentVars.put("courseName", lesson.getCourseName());
                        studentVars.put("location", location);
                        studentVars.put("teacherName", lesson.getTeacherName());
                        studentVars.put("studentName", studentName);
                        String body = fillTemplate(studentTemplate.getBody(), studentVars);
                        Map<String, String> studentSubjectVars = new HashMap<>();
                        studentSubjectVars.put("lessonDate", lessonDate);
                        studentSubjectVars.put("startTime", startTime);
                        studentSubjectVars.put("courseName", lesson.getCourseName());
                        String subject = fillTemplate(studentTemplate.getSubject(), studentSubjectVars);
                        sendEmail(student.getEmail(), subject, body);
                    }
                }
            }
        }
    }

    private SysEmailTemplate getTemplate(String code) {
        try {
            LambdaQueryWrapper<SysEmailTemplate> wrapper = new LambdaQueryWrapper<SysEmailTemplate>()
                    .eq(SysEmailTemplate::getTemplateCode, code)
                    .eq(SysEmailTemplate::getStatus, 1);
            return templateMapper.selectOne(wrapper);
        } catch (Exception e) {
            log.warn("获取邮件模板失败 code={}", code);
            return null;
        }
    }

    private List<SysParent> getStudentParents(Long studentId) {
        return parentMapper.selectByStudentId(studentId);
    }

    /**
     * 简单变量替换：${key} -> value
     */
    private String fillTemplate(String template, Map<String, String> vars) {
        if (template == null) return "";
        String result = template;
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            result = result.replace("${" + entry.getKey() + "}", entry.getValue() != null ? entry.getValue() : "");
        }
        return result;
    }

    private void sendEmail(String to, String subject, String body) {
        mailService.send(to, subject, body);
    }
}

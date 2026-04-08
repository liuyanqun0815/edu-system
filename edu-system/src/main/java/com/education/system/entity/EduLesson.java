package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

/**
 * 课程排课实体类
 * 
 * <p>记录具体课程安排的时间、地点和授课教师信息。</p>
 * 
 * <h3>核心字段说明：</h3>
 * <table border="1">
 *   <tr><th>字段</th><th>类型</th><th>说明</th></tr>
 *   <tr><td>lessonDate</td><td>LocalDate</td><td>上课日期</td></tr>
 *   <tr><td>startTime</td><td>LocalTime</td><td>上课开始时间</td></tr>
 *   <tr><td>endTime</td><td>LocalTime</td><td>上课结束时间</td></tr>
 *   <tr><td>location</td><td>String</td><td>上课地点（地址）</td></tr>
 *   <tr><td>status</td><td>Integer</td><td>状态：0取消 1正常</td></tr>
 *   <tr><td>notified</td><td>Integer</td><td>是否已发送提醒邮件</td></tr>
 * </table>
 * 
 * <h3>业务流程：</h3>
 * <pre>
 * 创建排课 → 定时检查 → 发送提醒邮件 → 上课 → 更新状态
 * </pre>
 * 
 * <h3>提醒邮件机制：</h3>
 * <p>系统定时任务会检查即将开始的课程（如提前1天/1小时），
 * 自动向教师和学生发送提醒邮件。已发送的课程notified字段置为1。</p>
 * 
 * <h3>通勤时间估算：</h3>
 * <p>location字段存储上课地址，可用于计算用户从常用地址（SysUser.address）
 * 到上课地点的通勤时间，帮助用户合理安排时间。</p>
 * 
 * <h3>关联实体：</h3>
 * <ul>
 *   <li>EduCourse - 课程（courseId）</li>
 *   <li>SysUser - 授课教师（teacherId）</li>
 * </ul>
 * 
 * @see SysUser 用户实体（教师信息）
 * @see com.education.system.scheduler.LessonEmailScheduler 排课邮件定时任务
 * @author education-team
 */
@Data
@TableName("edu_lesson")
public class EduLesson implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 课程ID */
    private Long courseId;

    /** 课程名称（冗余） */
    private String courseName;

    /** 授课教师用户ID */
    private Long teacherId;

    /** 教师姓名（冗余） */
    private String teacherName;

    /** 上课地点（地址，用于通勤估算） */
    private String location;

    /** 上课日期 */
    private LocalDate lessonDate;

    /** 上课开始时间 */
    private LocalTime startTime;

    /** 上课结束时间 */
    private LocalTime endTime;

    /** 状态：0取消 1正常 */
    private Integer status;

    /** 是否已发送提醒邮件：0否 1是 */
    private Integer notified;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}

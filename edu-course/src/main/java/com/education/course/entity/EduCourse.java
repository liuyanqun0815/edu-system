package com.education.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 课程实体类
 * 
 * <p>教培系统核心实体，存储课程的基本信息、分类、讲师关联和状态等。</p>
 * 
 * <h3>核心字段说明：</h3>
 * <table border="1">
 *   <tr><th>字段</th><th>说明</th><th>可选值</th></tr>
 *   <tr><td>difficulty</td><td>难度等级</td><td>1-初级 2-中级 3-高级</td></tr>
 *   <tr><td>status</td><td>课程状态</td><td>0-草稿 1-已发布 2-已下架</td></tr>
 *   <tr><td>categoryId</td><td>分类ID</td><td>关联edu_category表</td></tr>
 *   <tr><td>teacherId</td><td>讲师ID</td><td>关联sys_user表（角色为教师）</td></tr>
 * </table>
 * 
 * <h3>业务规则：</h3>
 * <ul>
 *   <li>课程创建后默认为草稿状态，发布后才可被学员看到</li>
 *   <li>已发布课程修改后需要重新审核或直接更新</li>
 *   <li>下架课程学员无法继续学习，但学习记录保留</li>
 *   <li>price为0表示免费课程</li>
 * </ul>
 * 
 * <h3>关联实体：</h3>
 * <ul>
 *   <li>{@link EduCategory} - 课程分类</li>
 *   <li>{@link EduChapter} - 课程章节（一对多）</li>
 *   <li>SysUser - 授课讲师（teacherId）</li>
 * </ul>
 * 
 * @see BaseEntity 实体基类（dr/createTime/updateTime）
 * @author education-team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_course")
public class EduCourse extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程编码
     */
    private String courseCode;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 大纲ID
     */
    private Long outlineId;

    /**
     * 讲师ID
     */
    private Long teacherId;

    /**
     * 课程封面
     */
    private String coverImage;

    /**
     * 课程简介
     */
    private String description;

    /**
     * 学习目标
     */
    private String learningObjectives;

    /**
     * 课程详情
     */
    private String detail;

    /**
     * 难度 1-初级 2-中级 3-高级
     */
    private Integer difficulty;

    /**
     * 课时数
     */
    private Integer courseHours;

    /**
     * 课程价格
     */
    private BigDecimal price;

    /**
     * 浏览次数
     */
    private Long viewCount;

    /**
     * 学习人数
     */
    private Long learnCount;

    /**
     * 平均评分(1-5星)
     */
    private BigDecimal rating;

    /**
     * 课程标签(逗号分隔)
     */
    private String tags;

    /**
     * 前置课程要求
     */
    private String prerequisites;

    /**
     * 目标受众
     */
    private String targetAudience;

    /**
     * 状态 0-草稿 1-已发布 2-已下架
     */
    private Integer status;
}

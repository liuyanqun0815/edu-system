package com.education.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 试卷实体类
 * 
 * <p>存储试卷的基本信息，包括分值、时长、题目数量等。</p>
 * 
 * <h3>核心字段说明：</h3>
 * <table border="1">
 *   <tr><th>字段</th><th>说明</th><th>备注</th></tr>
 *   <tr><td>paperType</td><td>试卷类型</td><td>1-练习卷 2-考试卷 3-模拟卷</td></tr>
 *   <tr><td>totalScore</td><td>总分</td><td>由组卷时自动计算</td></tr>
 *   <tr><td>passScore</td><td>及格分</td><td>判卷时使用</td></tr>
 *   <tr><td>duration</td><td>考试时长</td><td>单位：分钟</td></tr>
 *   <tr><td>questionCount</td><td>题目数量</td><td>由组卷时自动计算</td></tr>
 *   <tr><td>status</td><td>状态</td><td>0-草稿 1-已发布 2-已结束</td></tr>
 * </table>
 * 
 * <h3>关联关系：</h3>
 * <ul>
 *   <li>Grade - 年级（gradeId）</li>
 *   <li>Subject - 学科（subjectId）</li>
 *   <li>ExamPaperQuestion - 试卷题目关联（一对多）</li>
 *   <li>ExamRecord - 考试记录（一对多）</li>
 * </ul>
 * 
 * <h3>业务流程：</h3>
 * <pre>
 * 创建试卷 → 组卷(选择题目) → 发布 → 学员考试 → 自动/人工阅卷 → 归档
 * </pre>
 * 
 * @see ExamPaperQuestion 试卷题目关联
 * @see ExamRecord 考试记录
 * @author education-team
 */
@Data
@TableName("exam_paper")
public class ExamPaper {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private Long gradeId;
    
    private Long subjectId;
    
    private Integer paperType;
    
    private Integer totalScore;
    
    private Integer passScore;
    
    private Integer duration;
    
    private Integer questionCount;
    
    private String description;
    
    private Integer status;
    
    private Long createBy;
    
    /**
     * 逻辑删除：0-正常 1-删除
     */
    private Integer dr;
    
    private Date createTime;
    
    private Date updateTime;
}

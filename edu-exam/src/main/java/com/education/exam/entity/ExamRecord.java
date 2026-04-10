package com.education.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 考试记录实体类
 * 
 * <p>存储用户考试的详细信息，包括考试成绩、时长、答题情况等。</p>
 * 
 * <h3>防作弊字段：</h3>
 * <table border="1">
 *   <tr><th>字段</th><th>说明</th></tr>
 *   <tr><td>screenSwitchCount</td><td>切屏次数，用于检测作弊行为</td></tr>
 *   <tr><td>warningCount</td><td>警告次数，达到阈值可自动交卷</td></tr>
 *   <tr><td>browserInfo</td><td>浏览器信息，用于设备识别</td></tr>
 * </table>
 * 
 * @see BaseEntity 实体基类(dr/createTime/updateTime)
 * @author education-team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_record")
public class ExamRecord extends BaseEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 试卷ID
     */
    private Long paperId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 考试时长(分钟)
     */
    private Integer duration;
    
    /**
     * 总分
     */
    private Integer totalScore;
    
    /**
     * 得分
     */
    private Integer score;
    
    /**
     * 正确题数
     */
    private Integer correctCount;
    
    /**
     * 错误题数
     */
    private Integer wrongCount;
    
    /**
     * 状态 0-进行中 1-已完成 2-已批改
     */
    private Integer status;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * 浏览器信息
     */
    private String browserInfo;
    
    /**
     * 切屏次数
     */
    private Integer screenSwitchCount;
    
    /**
     * 警告次数
     */
    private Integer warningCount;
}

package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 评价实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_evaluation")
public class SysEvaluation extends BaseEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名称
     */
    private String userName;
    
    /**
     * 用户头像
     */
    private String userAvatar;
    
    /**
     * 评价对象ID（课程ID或考试ID）
     */
    private Long targetId;
    
    /**
     * 评价对象名称
     */
    private String targetName;
    
    /**
     * 评价对象类型：1课程 2考试
     */
    private Integer targetType;
    
    /**
     * 评分：1-5星
     */
    private Integer rating;
    
    /**
     * 评价内容
     */
    private String content;
    
    /**
     * 状态：0待回复 1已回复
     */
    private Integer status;
    
    /**
     * 回复内容
     */
    private String reply;
    
    /**
     * 回复时间
     */
    private Date replyTime;
}

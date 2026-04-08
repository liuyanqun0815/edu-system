package com.education.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@TableName("question")
public class Question {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long subjectId;
    
    private Long gradeId;
    
    private String title;
    
    private Integer questionType;
    
    private Integer difficulty;
    
    private Integer score;
    
    private String answer;
    
    private String analysis;
    
    private String knowledgePoint;
    
    private String source;
    
    private Integer usageCount;
    
    private Integer status;
    
    /**
     * 逻辑删除：0-正常 1-删除
     */
    private Integer dr;
    
    private Date createTime;
    
    private Date updateTime;
    
    private List<QuestionOption> options;
}

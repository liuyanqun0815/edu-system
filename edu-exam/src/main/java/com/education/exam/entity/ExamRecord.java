package com.education.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("exam_record")
public class ExamRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long paperId;
    
    private Long userId;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Integer duration;
    
    private Integer totalScore;
    
    private Integer score;
    
    private Integer correctCount;
    
    private Integer wrongCount;
    
    private Integer status;
    
    private String ipAddress;
}

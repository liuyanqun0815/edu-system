package com.education.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("exam_paper_question")
public class ExamPaperQuestion {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long paperId;
    
    private Long questionId;
    
    private Integer questionOrder;
    
    private Integer score;
}

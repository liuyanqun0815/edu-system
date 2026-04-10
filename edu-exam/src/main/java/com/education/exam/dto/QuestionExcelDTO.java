package com.education.exam.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

/**
 * 题目导入导出DTO
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(25)
@ColumnWidth(25)
public class QuestionExcelDTO {

    @ExcelProperty(value = "题目内容*", index = 0)
    private String questionTitle;

    @ExcelProperty(value = "题型* (1-单选 2-多选 3-判断 4-填空 5-简答)", index = 1)
    private Integer questionType;

    @ExcelProperty(value = "正确答案*", index = 2)
    private String correctAnswer;

    @ExcelProperty(value = "选项 (选择题填写,格式: A.xxx B.xxx C.xxx D.xxx)", index = 3)
    private String options;

    @ExcelProperty(value = "学科ID", index = 4)
    private Long subjectId;

    @ExcelProperty(value = "年级", index = 5)
    private String grade;

    @ExcelProperty(value = "考试类型 (1-单元测试 2-期中 3-期末 4-模拟考 5-真题 6-课后练习)", index = 6)
    private Integer examType;

    @ExcelProperty(value = "难度 (1-简单 2-中等 3-困难)", index = 7)
    private Integer difficulty;

    @ExcelProperty(value = "分值", index = 8)
    private Double score;

    @ExcelProperty(value = "知识点标签", index = 9)
    private String tags;

    @ExcelProperty(value = "答案解析", index = 10)
    private String analysis;
}

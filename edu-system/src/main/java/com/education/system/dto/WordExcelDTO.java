package com.education.system.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 单词Excel导入DTO
 */
@Data
public class WordExcelDTO {

    /**
     * 英文单词（必填）
     */
    @ExcelProperty(value = "英文单词*", index = 0)
    @NotBlank(message = "英文单词不能为空")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "英文单词只能包含字母")
    private String word;

    /**
     * 音标
     */
    @ExcelProperty(value = "音标", index = 1)
    private String phonetic;

    /**
     * 中文释义（必填）
     */
    @ExcelProperty(value = "中文释义*", index = 2)
    @NotBlank(message = "中文释义不能为空")
    private String translation;

    /**
     * 年级
     */
    @ExcelProperty(value = "年级", index = 3)
    private String grade;

    /**
     * 难度（1-简单，2-中等，3-困难）
     */
    @ExcelProperty(value = "难度", index = 4)
    private Integer difficulty;

    /**
     * 例句
     */
    @ExcelProperty(value = "例句", index = 5)
    private String example;
}

package com.education.system.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 训练配置DTO
 */
@Data
public class TrainingConfigDTO {

    /**
     * 年级(如:六年级、初一、高三)
     */
    @NotBlank(message = "年级不能为空")
    private String grade;

    /**
     * 难度:1-简单 2-中等 3-困难
     */
    @NotNull(message = "难度不能为空")
    @Min(value = 1, message = "难度最小为1")
    @Max(value = 3, message = "难度最大为3")
    private Integer difficulty;

    /**
     * 题目类型:spell-拼写,choice_select-看词选义,choice_word-看义选词,translate-翻译,sentence_fill-例句填空
     */
    @NotBlank(message = "题目类型不能为空")
    private String questionType;

    /**
     * 题目数量
     */
    @NotNull(message = "题目数量不能为空")
    @Min(value = 5, message = "题目数量最少为5")
    @Max(value = 100, message = "题目数量最多为100")
    private Integer questionCount;

    /**
     * 每题时间(秒),默认30秒
     */
    @Min(value = 10, message = "每题时间最少10秒")
    @Max(value = 120, message = "每题时间最多120秒")
    private Integer perQuestionTime = 30;
}

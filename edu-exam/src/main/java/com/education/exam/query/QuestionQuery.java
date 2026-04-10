package com.education.exam.query;

import com.education.common.query.BaseQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 题目查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("题目查询参数")
public class QuestionQuery extends BaseQuery {

    private static final long serialVersionUID = 1L;

    /**
     * 题目标题（模糊搜索）
     */
    @ApiModelProperty("题目标题")
    private String questionTitle;

    /**
     * 题型（1单选/2多选/3判断/4填空/5简答）
     */
    @ApiModelProperty("题型")
    private Integer questionType;

    /**
     * 学科ID
     */
    @ApiModelProperty("学科ID")
    private Long subjectId;

    /**
     * 年级
     */
    @ApiModelProperty("年级")
    private String grade;

    /**
     * 考试类型（1-单元测试 2-期中 3-期末 4-模拟考 5-真题 6-课后练习）
     */
    @ApiModelProperty("考试类型")
    private Integer examType;

    /**
     * 难度等级
     */
    @ApiModelProperty("难度等级")
    private Integer difficulty;

    /**
     * 知识点
     */
    @ApiModelProperty("知识点")
    private String knowledgePoint;

    /**
     * 状态（0禁用/1启用）
     */
    @ApiModelProperty("状态")
    private Integer status;

    /**
     * 题目来源类型（1手动/2导入/3解析）
     */
    @ApiModelProperty("题目来源类型")
    private Integer sourceType;

    /**
     * 题目标签（逗号分隔）
     */
    @ApiModelProperty("题目标签")
    private String tags;
}

package com.education.system.query;

import com.education.common.query.BaseQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评价查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("评价查询参数")
public class EvaluationQuery extends BaseQuery {

    private static final long serialVersionUID = 1L;

    /**
     * 评价内容（模糊搜索）
     */
    @ApiModelProperty("评价内容")
    private String content;

    /**
     * 评分（1-5星）
     */
    @ApiModelProperty("评分")
    private Integer rating;

    /**
     * 状态（0待回复/1已回复）
     */
    @ApiModelProperty("状态")
    private Integer status;

    /**
     * 评价对象类型（1课程/2考试）
     */
    @ApiModelProperty("评价对象类型")
    private Integer targetType;

    /**
     * 评价对象ID
     */
    @ApiModelProperty("评价对象ID")
    private Long targetId;
}

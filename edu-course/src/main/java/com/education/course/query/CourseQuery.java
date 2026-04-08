package com.education.course.query;

import com.education.common.query.BaseQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("课程查询参数")
public class CourseQuery extends BaseQuery {

    private static final long serialVersionUID = 1L;

    /**
     * 课程名称（模糊搜索）
     */
    @ApiModelProperty("课程名称")
    private String courseName;

    /**
     * 分类ID
     */
    @ApiModelProperty("分类ID")
    private Long categoryId;

    /**
     * 教师ID
     */
    @ApiModelProperty("教师ID")
    private Long teacherId;

    /**
     * 状态（0草稿/1发布/2下架）
     */
    @ApiModelProperty("状态")
    private Integer status;

    /**
     * 最低价格
     */
    @ApiModelProperty("最低价格")
    private java.math.BigDecimal minPrice;

    /**
     * 最高价格
     */
    @ApiModelProperty("最高价格")
    private java.math.BigDecimal maxPrice;
}

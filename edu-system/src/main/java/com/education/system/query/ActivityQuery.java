package com.education.system.query;

import com.education.common.query.BaseQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 活动查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ActivityQuery extends BaseQuery {

    private static final long serialVersionUID = 1L;

    /**
     * 活动标题
     */
    private String title;

    /**
     * 活动类型：1系统公告 2课程相关 3考试相关 4用户相关
     */
    private Integer activityType;

    /**
     * 状态：0草稿 1已发布 2已结束
     */
    private Integer status;
}

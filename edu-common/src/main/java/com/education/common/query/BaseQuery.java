package com.education.common.query;

import lombok.Data;

import java.io.Serializable;

/**
 * 基础查询参数
 */
@Data
public class BaseQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private Long pageNum = 1L;

    /**
     * 每页大小
     */
    private Long pageSize = 10L;
}

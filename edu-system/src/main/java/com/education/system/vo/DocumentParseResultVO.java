package com.education.system.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 文档解析结果VO
 */
@Data
public class DocumentParseResultVO {

    /**
     * 解析是否成功
     */
    private Boolean success;

    /**
     * 解析消息
     */
    private String message;

    /**
     * 解析出的数据条数
     */
    private Integer totalCount;

    /**
     * 解析出的数据列表
     */
    private List<Map<String, Object>> dataList;

    /**
     * 解析警告信息
     */
    private List<String> warnings;
}

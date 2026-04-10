package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文档解析模板配置实体
 */
@Data
@TableName("sys_parse_template")
public class SysParseTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板编码(唯一标识)
     */
    private String templateCode;

    /**
     * 文档类型: word-单词, question-题目, course-课程, student-学生, teacher-教师
     */
    private String documentType;

    /**
     * 业务模块: sys_word, exam_question, edu_course, sys_user等
     */
    private String businessModule;

    /**
     * 解析规则(JSON格式)
     * 示例: {
     *   "pattern": "^\\s*([a-zA-Z]+)\\s+(?:\\[([^\\]]+)\\]\\s+)?([\\u4e00-\\u9fa5]+)",
     *   "fields": ["word", "phonetic", "translation"],
     *   "defaults": {"grade": "九年级", "difficulty": 2}
     * }
     */
    private String parseRules;

    /**
     * 示例文档内容
     */
    private String exampleContent;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 备注说明
     */
    private String remark;

    private Date createTime;
    private Date updateTime;
    private Integer dr;
}

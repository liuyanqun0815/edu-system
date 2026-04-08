package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 单词训练实体
 */
@Data
@TableName("sys_word")
public class SysWord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 英文单词
     */
    private String word;

    /**
     * 音标
     */
    private String phonetic;

    /**
     * 中文释义
     */
    private String translation;

    /**
     * 年级（如：六年级、初一、高三）
     */
    private String grade;

    /**
     * 难度 1-简单 2-中等 3-困难
     */
    private Integer difficulty;

    /**
     * 例句
     */
    private String example;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}

package com.education.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 题库实体类
 * 
 * <p>存储各类题目的内容、答案、解析等信息，支持多种题型。</p>
 * 
 * <h3>题型说明：</h3>
 * <table border="1">
 *   <tr><th>题型代码</th><th>题型名称</th><th>答案格式</th></tr>
 *   <tr><td>1</td><td>单选题</td><td>单个字母，如：A</td></tr>
 *   <tr><td>2</td><td>多选题</td><td>多个字母，如：ABC</td></tr>
 *   <tr><td>3</td><td>判断题</td><td>T/F 或 1/0</td></tr>
 *   <tr><td>4</td><td>填空题</td><td>答案文本，多个空用||分隔</td></tr>
 *   <tr><td>5</td><td>简答题</td><td>参考答案文本</td></tr>
 * </table>
 * 
 * <h3>难度等级：</h3>
 * <ul>
 *   <li>1 - 简单（基础知识点）</li>
 *   <li>2 - 中等（综合应用）</li>
 *   <li>3 - 困难（拓展延伸）</li>
 * </ul>
 * 
 * <h3>选项存储格式：</h3>
 * <pre>
 * options字段存储JSON数组：
 * [{"label":"A","content":"选项A内容"},{"label":"B","content":"选项B内容"}]
 * </pre>
 * 
 * <h3>知识点格式：</h3>
 * <p>支持多个知识点，用逗号或分号分隔，如："代数,方程,一元二次方程"</p>
 * 
 * @see BaseEntity 实体基类
 * @see ExamSubject 学科实体
 * @author education-team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_question")
public class ExamQuestion extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 学科ID
     */
    private Long subjectId;

    /**
     * 所属年级，如：六年级、初一、高三
     */
    private String grade;

    /**
     * 题型 1-单选 2-多选 3-判断 4-填空 5-简答
     */
    private Integer questionType;

    /**
     * 题目内容
     */
    private String questionTitle;

    /**
     * 选项 JSON格式
     */
    private String options;

    /**
     * 正确答案
     */
    private String correctAnswer;

    /**
     * 答案解析
     */
    private String analysis;

    /**
     * 难度 1-简单 2-中等 3-困难
     */
    private Integer difficulty;

    /**
     * 默认分值
     */
    private BigDecimal score;

    /**
     * 知识点
     */
    private String knowledgePoint;

    /**
     * 使用次数
     */
    private Integer usageCount;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;
}

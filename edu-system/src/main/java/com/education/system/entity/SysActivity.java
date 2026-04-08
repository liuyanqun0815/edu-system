package com.education.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 活动公告实体类
 * 
 * <p>存储系统公告、活动通知等信息，支持多种活动类型和状态管理。</p>
 * 
 * <h3>活动类型：</h3>
 * <table border="1">
 *   <tr><th>activityType</th><th>类型名称</th><th>用途</th></tr>
 *   <tr><td>1</td><td>系统公告</td><td>系统维护、更新通知等</td></tr>
 *   <tr><td>2</td><td>课程相关</td><td>新课程上线、课程优惠等</td></tr>
 *   <tr><td>3</td><td>考试相关</td><td>考试安排、成绩公布等</td></tr>
 *   <tr><td>4</td><td>用户相关</td><td>活动促销、用户福利等</td></tr>
 * </table>
 * 
 * <h3>状态流转：</h3>
 * <pre>
 * 草稿(0) → 已发布(1) → 已结束(2)
 * </pre>
 * 
 * <h3>置顶功能：</h3>
 * <p>isTop=1的活动会优先显示在列表顶部，用于重要通知的突出展示。</p>
 * 
 * <h3>时效性：</h3>
 * <p>startTime和endTime定义活动的有效时间范围，过期活动自动标记为已结束。</p>
 * 
 * @author education-team
 */
@Data
@TableName("sys_activity")
public class SysActivity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 活动标题
     */
    private String title;

    /**
     * 活动内容
     */
    private String content;

    /**
     * 活动类型：1系统公告 2课程相关 3考试相关 4用户相关
     */
    private Integer activityType;

    /**
     * 图标
     */
    private String icon;

    /**
     * 颜色
     */
    private String color;

    /**
     * 状态：0草稿 1已发布 2已结束
     */
    private Integer status;

    /**
     * 是否置顶：0否 1是
     */
    private Integer isTop;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 发布时间
     */
    private Date publishTime;

    /**
     * 创建人 ID
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}

package com.education.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程章节实体类
 * 
 * <p>存储课程的章节信息，每个章节对应一个视频或学习内容。</p>
 * 
 * <h3>核心字段说明：</h3>
 * <table border="1">
 *   <tr><th>字段</th><th>说明</th></tr>
 *   <tr><td>courseId</td><td>所属课程ID</td></tr>
 *   <tr><td>outlineId</td><td>关联大纲ID（可选）</td></tr>
 *   <tr><td>videoUrl</td><td>视频播放地址</td></tr>
 *   <tr><td>videoDuration</td><td>视频时长（秒）</td></tr>
 *   <tr><td>isFree</td><td>是否免费试看</td></tr>
 * </table>
 * 
 * <h3>章节排序：</h3>
 * <p>sortOrder字段控制章节在课程中的显示顺序，
 * 前端按此字段升序排列展示章节列表。</p>
 * 
 * <h3>免费试看：</h3>
 * <p>isFree=1的章节可以免费观看，用于吸引用户购买课程。</p>
 * 
 * @see EduCourse 课程实体
 * @see BaseEntity 实体基类
 * @author education-team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_chapter")
public class EduChapter extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 大纲ID
     */
    private Long outlineId;

    /**
     * 章节名称
     */
    private String chapterName;

    /**
     * 章节描述
     */
    private String description;

    /**
     * 视频地址
     */
    private String videoUrl;

    /**
     * 视频时长(秒)
     */
    private Integer videoDuration;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 是否免费 0-否 1-是
     */
    private Integer isFree;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;
}

package com.education.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.education.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 课程分类实体类
 * 
 * <p>存储课程分类信息，支持树形层级结构。</p>
 * 
 * <h3>树形结构：</h3>
 * <pre>
 * parentId=0 表示根节点
 * 根分类
 * ├── 编程语言(parentId=0)
 * │   ├── Java(parentId=1)
 * │   └── Python(parentId=1)
 * └── 学科辅导(parentId=0)
 *     ├── 数学(parentId=3)
 *     └── 英语(parentId=3)
 * </pre>
 * 
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>课程列表分类筛选</li>
 *   <li>导航菜单分类展示</li>
 *   <li>按分类统计课程数量</li>
 * </ul>
 * 
 * @see EduCourse 课程实体
 * @see BaseEntity 实体基类
 * @author education-team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_category")
public class EduCategory extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类编码
     */
    private String categoryCode;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 子分类（非数据库字段，树形结构用）
     */
    @TableField(exist = false)
    private List<EduCategory> children;
}

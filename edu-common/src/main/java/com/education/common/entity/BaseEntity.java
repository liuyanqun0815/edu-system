package com.education.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 实体基类
 * 
 * <p>所有业务实体类的父类，封装通用字段和MyBatis-Plus注解配置。</p>
 * 
 * <h3>核心功能：</h3>
 * <ul>
 *   <li>逻辑删除：通过 {@code @TableLogic} 注解，删除操作自动转为UPDATE dr=1</li>
 *   <li>自动填充：创建时间和更新时间由MyBatis-Plus自动填充，无需手动设置</li>
 *   <li>序列化支持：实现Serializable接口，支持分布式环境下的对象传输</li>
 * </ul>
 * 
 * <h3>使用示例：</h3>
 * <pre>{@code
 * @Data
 * @EqualsAndHashCode(callSuper = true)
 * @TableName("sys_user")
 * public class SysUser extends BaseEntity {
 *     @TableId(type = IdType.AUTO)
 *     private Long id;
 *     private String username;
 * }
 * }</pre>
 * 
 * <h3>注意事项：</h3>
 * <ul>
 *   <li>继承此类后，查询时自动过滤 dr=1 的记录</li>
 *   <li>调用 removeById() 实际执行 UPDATE ... SET dr=1</li>
 *   <li>数据库表必须包含 dr、create_time、update_time 字段</li>
 * </ul>
 * 
 * @see com.education.common.config.MybatisPlusConfig#metaObjectHandler() 自动填充配置
 * @author education-team
 */
@Data
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 删除标志
     * <ul>
     *   <li>0 - 正常状态（默认值，由MyBatis-Plus自动填充）</li>
     *   <li>1 - 已删除（逻辑删除标记）</li>
     * </ul>
     * 
     * <p>配合 @TableLogic 注解，调用 removeById() 时自动将此字段置为1，
     * 查询时自动添加 WHERE dr=0 条件</p>
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer dr;

    /**
     * 创建时间
     * 
     * <p>记录首次插入的时间，由MyBatis-Plus在INSERT时自动填充当前时间。</p>
     * <p>注意：手动设置此字段无效，会被自动填充覆盖。</p>
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     * 
     * <p>记录最后修改的时间，由MyBatis-Plus在INSERT和UPDATE时自动填充当前时间。</p>
     * <p>每次更新记录时此字段会自动刷新为当前时间。</p>
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}

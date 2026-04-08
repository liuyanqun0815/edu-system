package com.education.system.query;

import com.education.common.query.BaseQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserQuery extends BaseQuery {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 角色ID（按角色过滤）
     */
    private Long roleId;

    /**
     * 上级用户ID（按层级过滤）
     */
    private Long parentId;

    /**
     * 可见用户ID列表（权限控制）
     */
    private java.util.Set<Long> visibleUserIds;
}

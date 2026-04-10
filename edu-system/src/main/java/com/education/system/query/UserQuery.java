package com.education.system.query;

import com.education.common.query.BaseQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

/**
 * 用户查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("用户查询参数")
public class UserQuery extends BaseQuery {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名（模糊搜索）
     */
    @ApiModelProperty("用户名")
    private String username;

    /**
     * 昵称（模糊搜索）
     */
    @ApiModelProperty("昵称")
    private String nickname;

    /**
     * 状态（0禁用/1启用）
     */
    @ApiModelProperty("状态")
    private Integer status;

    /**
     * 角色ID
     */
    @ApiModelProperty("角色ID")
    private Long roleId;

    /**
     * 可见用户ID集合（权限控制）
     */
    @ApiModelProperty(hidden = true)
    private Set<Long> visibleUserIds;
}

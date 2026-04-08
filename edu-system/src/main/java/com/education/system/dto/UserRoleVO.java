package com.education.system.dto;

import lombok.Data;

/**
 * 用户角色VO
 */
@Data
public class UserRoleVO {
    
    /**
     * 角色ID
     */
    private Long roleId;
    
    /**
     * 角色名称
     */
    private String roleName;
    
    /**
     * 角色编码
     */
    private String roleCode;
}

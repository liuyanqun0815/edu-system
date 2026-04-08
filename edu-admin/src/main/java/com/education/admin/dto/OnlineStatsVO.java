package com.education.admin.dto;

import lombok.Data;

import java.util.List;

/**
 * 在线统计 VO
 */
@Data
public class OnlineStatsVO {
    
    /**
     * 总在线人数
     */
    private Integer totalOnline;
    
    /**
     * 可见在线人数
     */
    private Integer visibleOnline;
    
    /**
     * 角色统计列表
     */
    private List<RoleStatItemVO> roleStats;
    
    /**
     * 用户列表
     */
    private List<OnlineUserVO> userList;
    
    /**
     * 是否可见
     */
    private Boolean isVisible;
    
    /**
     * 当前用户角色等级
     */
    private Integer currentUserRoleLevel;
    
    @Data
    public static class RoleStatItemVO {
        private Long roleId;
        private String roleName;
        private String roleCode;
        private Integer roleLevel;
        private Integer count;
    }
    
    @Data
    public static class OnlineUserVO {
        private Long id;
        private String username;
        private String nickname;
        private String roleName;
        private Integer roleLevel;
        private Long parentId;
        private String parentName;
    }
}

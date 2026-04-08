package com.education.system.dto;

import lombok.Data;

import java.util.List;

/**
 * 设置分组 VO
 */
@Data
public class SettingGroupVO {
    
    /**
     * 分组编码
     */
    private String groupCode;
    
    /**
     * 分组名称
     */
    private String groupName;
    
    /**
     * 设置项列表
     */
    private List<SettingItemVO> items;
    
    @Data
    public static class SettingItemVO {
        /**
         * 设置键
         */
        private String settingKey;
        
        /**
         * 设置值
         */
        private String settingValue;
    }
}

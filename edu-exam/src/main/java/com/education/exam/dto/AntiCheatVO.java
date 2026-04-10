package com.education.exam.dto;

import lombok.Data;

/**
 * 防作弊信息VO
 */
@Data
public class AntiCheatVO {

    /**
     * 浏览器信息
     */
    private String browserInfo;

    /**
     * 切屏次数
     */
    private Integer screenSwitchCount;

    /**
     * 警告次数
     */
    private Integer warningCount;

    /**
     * 是否有作弊行为
     */
    public Boolean hasCheating() {
        return (screenSwitchCount != null && screenSwitchCount > 5) 
            || (warningCount != null && warningCount > 3);
    }
}

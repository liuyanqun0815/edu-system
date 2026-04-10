package com.education.system.strategy;

import com.education.system.dto.ParseResult;

/**
 * 解析策略接口
 * 预留AI扩展点，支持rule/ai策略切换
 */
public interface ParseStrategy {

    /**
     * 解析HTML内容
     * @param html HTML内容
     * @return 解析结果
     */
    ParseResult parse(String html);

    /**
     * 是否支持该源类型
     * @param sourceType 源类型
     * @return 是否支持
     */
    boolean supports(String sourceType);

    /**
     * 策略名称
     * @return 策略名称
     */
    String getStrategyName();
}

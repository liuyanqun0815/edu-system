package com.education.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.system.entity.SysConfig;

/**
 * 系统配置Service接口
 *
 * @author system
 * @since 2026-04-09
 */
public interface ISysConfigService extends IService<SysConfig> {

    /**
     * 根据配置键获取配置值
     *
     * @param configKey 配置键
     * @return 配置值
     */
    String getConfigValue(String configKey);

    /**
     * 更新配置值
     *
     * @param configKey 配置键
     * @param configValue 配置值
     */
    void updateConfigValue(String configKey, String configValue);

    /**
     * 清除配置缓存
     */
    void clearConfigCache();
}

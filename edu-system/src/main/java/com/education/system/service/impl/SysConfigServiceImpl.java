package com.education.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.system.entity.SysConfig;
import com.education.system.mapper.SysConfigMapper;
import com.education.system.service.ISysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统配置Service实现类
 *
 * @author system
 * @since 2026-04-09
 */
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig>
        implements ISysConfigService {

    private static final String CONFIG_CACHE_NAME = "sysConfig";

    /**
     * 重写save方法,清除配置缓存
     */
    @Override
    @Caching(evict = {
        @CacheEvict(value = CONFIG_CACHE_NAME, allEntries = true)
    })
    public boolean save(SysConfig entity) {
        return super.save(entity);
    }

    /**
     * 重写updateById方法,清除配置缓存
     */
    @Override
    @Caching(evict = {
        @CacheEvict(value = CONFIG_CACHE_NAME, allEntries = true)
    })
    public boolean updateById(SysConfig entity) {
        return super.updateById(entity);
    }

    /**
     * 重写removeById方法,清除配置缓存
     */
    @Override
    @Caching(evict = {
        @CacheEvict(value = CONFIG_CACHE_NAME, allEntries = true)
    })
    public boolean removeById(java.io.Serializable id) {
        return super.removeById(id);
    }

    @Override
    @Cacheable(value = CONFIG_CACHE_NAME, key = "#configKey")
    public String getConfigValue(String configKey) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, configKey);

        SysConfig config = getOne(wrapper);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    @CacheEvict(value = CONFIG_CACHE_NAME, key = "#configKey")
    @Transactional(rollbackFor = Exception.class)
    public void updateConfigValue(String configKey, String configValue) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, configKey);

        SysConfig config = getOne(wrapper);
        if (config != null) {
            config.setConfigValue(configValue);
            updateById(config);
        }
    }

    @Override
    @CacheEvict(value = CONFIG_CACHE_NAME, allEntries = true)
    public void clearConfigCache() {
        // 缓存已由@CacheEvict自动清除
    }
}

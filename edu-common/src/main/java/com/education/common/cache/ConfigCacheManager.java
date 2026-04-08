package com.education.common.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统配置缓存管理器（单例模式）
 * 
 * <p>使用双重检查锁实现线程安全的单例，避免重复查询数据库。</p>
 * 
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>系统参数频繁读取（如系统名称、Logo、邮件配置等）</li>
 *   <li>配置更新时自动刷新缓存</li>
 * </ul>
 * 
 * <h3>示例：</h3>
 * <pre>
 * String systemName = ConfigCacheManager.getInstance().getConfig("system.name");
 * </pre>
 * 
 * <h3>注意：</h3>
 * <p>本类需要在edu-system模块中通过配置Service调用refreshCache()方法加载数据。</p>
 */
@Slf4j
@Component
public class ConfigCacheManager {

    /**
     * 缓存存储
     */
    private final Map<String, String> configCache = new ConcurrentHashMap<>();

    /**
     * 缓存是否已初始化
     */
    private volatile boolean initialized = false;

    /**
     * 应用启动时初始化（需手动调用refreshCache加载数据）
     */
    @PostConstruct
    public void init() {
        log.info("ConfigCacheManager初始化完成，等待数据加载...");
    }

    /**
     * 获取配置值
     * 
     * @param configKey 配置键
     * @return 配置值，不存在返回null
     */
    public String getConfig(String configKey) {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    log.warn("配置缓存未初始化，返回null");
                    return null;
                }
            }
        }
        return configCache.get(configKey);
    }

    /**
     * 获取配置值（带默认值）
     * 
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public String getConfig(String configKey, String defaultValue) {
        String value = getConfig(configKey);
        return value != null ? value : defaultValue;
    }

    /**
     * 刷新缓存（配置更新时调用）
     * 
     * @param configs 配置数据Map
     */
    public synchronized void refreshCache(Map<String, String> configs) {
        log.info("开始刷新系统配置缓存...");
        configCache.clear();
        
        try {
            if (configs != null) {
                configCache.putAll(configs);
            }
            initialized = true;
            log.info("系统配置缓存刷新完成，共加载{}条配置", configCache.size());
        } catch (Exception e) {
            log.error("系统配置缓存刷新失败", e);
            initialized = false;
        }
    }

    /**
     * 更新单个配置缓存
     * 
     * @param configKey 配置键
     * @param configValue 配置值
     */
    public void updateConfig(String configKey, String configValue) {
        configCache.put(configKey, configValue);
    }

    /**
     * 删除配置缓存
     * 
     * @param configKey 配置键
     */
    public void removeConfig(String configKey) {
        configCache.remove(configKey);
    }

    /**
     * 获取所有配置
     * 
     * @return 配置Map
     */
    public Map<String, String> getAllConfigs() {
        return new ConcurrentHashMap<>(configCache);
    }
}

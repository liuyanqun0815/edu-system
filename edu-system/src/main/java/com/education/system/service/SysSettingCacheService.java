package com.education.system.service;

import com.education.system.entity.SysSetting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 系统设置缓存服务
 * 
 * 功能:
 * 1. 本地缓存 sys_setting 表数据,提高读取性能
 * 2. 定时刷新缓存(每5分钟)
 * 3. 支持手动刷新
 * 4. 提供类型安全的配置读取方法
 * 
 * 使用场景:
 * - 需要频繁读取的配置(如验证码参数)
 * - 不需要实时性特别高的配置
 * 
 * 注意: 如需实时刷新,请使用 DynamicConfigService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysSettingCacheService {

    private final ISysSettingService settingService;

    /**
     * 本地缓存
     * 结构: Map<groupCode, Map<key, value>>
     */
    private final Map<String, Map<String, String>> cache = new ConcurrentHashMap<>();

    /**
     * 最后刷新时间
     */
    private volatile long lastRefreshTime = 0;

    /**
     * 缓存有效期(毫秒): 5分钟
     */
    private static final long CACHE_TTL = 5 * 60 * 1000;

    /**
     * 初始化: 加载配置到缓存
     */
    @PostConstruct
    public void init() {
        refreshCache();
        log.info("系统设置缓存服务初始化完成");
    }

    /**
     * 刷新缓存
     * 加载所有启用的配置到本地缓存
     */
    public synchronized void refreshCache() {
        try {
            List<SysSetting> settings = settingService.listAll();
            
            // 按分组组织配置
            Map<String, Map<String, String>> newCache = settings.stream()
                .collect(Collectors.groupingBy(
                    SysSetting::getGroupCode,
                    Collectors.toMap(
                        SysSetting::getSettingKey,
                        s -> s.getSettingValue() != null ? s.getSettingValue() : "",
                        (v1, v2) -> v1
                    )
                ));
            
            cache.clear();
            cache.putAll(newCache);
            lastRefreshTime = System.currentTimeMillis();
            
            int totalCount = newCache.values().stream().mapToInt(Map::size).sum();
            log.info("系统设置缓存已刷新,共 {} 个分组, {} 条配置", newCache.size(), totalCount);
        } catch (Exception e) {
            log.error("刷新系统设置缓存失败", e);
        }
    }

    /**
     * 检查缓存是否需要刷新
     */
    private void checkAndRefresh() {
        if (System.currentTimeMillis() - lastRefreshTime > CACHE_TTL) {
            refreshCache();
        }
    }

    /**
     * 定时刷新缓存(每5分钟)
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void scheduledRefresh() {
        log.debug("定时刷新系统设置缓存...");
        refreshCache();
    }

    // =========================================
    // 配置读取方法
    // =========================================

    /**
     * 获取字符串配置
     * 
     * @param group 分组编码
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public String getString(String group, String key, String defaultValue) {
        checkAndRefresh();
        
        Map<String, String> groupMap = cache.get(group);
        if (groupMap == null) {
            return defaultValue;
        }
        
        String value = groupMap.get(key);
        return StringUtils.isNotBlank(value) ? value : defaultValue;
    }

    /**
     * 获取字符串配置(无默认值)
     */
    public String getString(String group, String key) {
        return getString(group, key, null);
    }

    /**
     * 获取整数配置
     */
    public int getInt(String group, String key, int defaultValue) {
        String value = getString(group, key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("配置值转整数失败: {}.{}, value={}", group, key, value);
            return defaultValue;
        }
    }

    /**
     * 获取长整数配置
     */
    public long getLong(String group, String key, long defaultValue) {
        String value = getString(group, key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            log.warn("配置值转长整数失败: {}.{}, value={}", group, key, value);
            return defaultValue;
        }
    }

    /**
     * 获取布尔配置
     * 支持: 1/0, true/false, yes/no
     */
    public boolean getBoolean(String group, String key, boolean defaultValue) {
        String value = getString(group, key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        
        String lowerValue = value.trim().toLowerCase();
        return "1".equals(lowerValue) || 
               "true".equals(lowerValue) || 
               "yes".equals(lowerValue);
    }

    /**
     * 获取双精度浮点数配置
     */
    public double getDouble(String group, String key, double defaultValue) {
        String value = getString(group, key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            log.warn("配置值转浮点数失败: {}.{}, value={}", group, key, value);
            return defaultValue;
        }
    }

    /**
     * 获取字符串数组配置(逗号分隔)
     */
    public String[] getArray(String group, String key, String[] defaultValue) {
        String value = getString(group, key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        
        return value.split(",");
    }

    // =========================================
    // 分组配置读取
    // =========================================

    /**
     * 获取整个分组的配置
     */
    public Map<String, String> getGroup(String group) {
        checkAndRefresh();
        return cache.getOrDefault(group, new ConcurrentHashMap<>());
    }

    /**
     * 检查分组是否存在
     */
    public boolean hasGroup(String group) {
        checkAndRefresh();
        return cache.containsKey(group);
    }

    /**
     * 检查配置项是否存在
     */
    public boolean hasKey(String group, String key) {
        checkAndRefresh();
        Map<String, String> groupMap = cache.get(group);
        return groupMap != null && groupMap.containsKey(key);
    }

    // =========================================
    // 便捷方法: 按分组封装
    // =========================================

    /**
     * 获取站点名称
     */
    public String getSiteName() {
        return getString("basic", "siteName", "智慧教培管理系统");
    }

    /**
     * 获取版权信息
     */
    public String getSiteCopyright() {
        return getString("basic", "siteCopyright", "© 2024 智慧教培");
    }

    /**
     * 是否启用验证码
     */
    public boolean isCaptchaEnabled() {
        return getBoolean("security", "captchaEnabled", true);
    }

    /**
     * 获取验证码长度
     */
    public int getCaptchaLength() {
        return getInt("security", "captchaLength", 4);
    }

    /**
     * 获取验证码宽度
     */
    public int getCaptchaWidth() {
        return getInt("security", "captchaWidth", 160);
    }

    /**
     * 获取验证码高度
     */
    public int getCaptchaHeight() {
        return getInt("security", "captchaHeight", 60);
    }

    /**
     * 获取验证码过期时间(分钟)
     */
    public int getCaptchaExpireMinutes() {
        return getInt("security", "captchaExpire", 5);
    }

    /**
     * 获取密码最小长度
     */
    public int getPasswordMinLength() {
        return getInt("security", "passwordMinLength", 6);
    }

    /**
     * 获取密码最大长度
     */
    public int getPasswordMaxLength() {
        return getInt("security", "passwordMaxLength", 20);
    }

    /**
     * 获取登录失败锁定次数
     */
    public int getLoginFailLockCount() {
        return getInt("security", "loginFailLockCount", 5);
    }

    /**
     * 获取登录锁定时间(分钟)
     */
    public int getLoginFailLockTime() {
        return getInt("security", "loginFailLockTime", 30);
    }

    // =========================================
    // 缓存管理
    // =========================================

    /**
     * 获取缓存统计信息
     */
    public String getCacheStats() {
        int groupCount = cache.size();
        int totalCount = cache.values().stream().mapToInt(Map::size).sum();
        return String.format("分组数: %d, 配置项: %d, 最后刷新: %s", 
            groupCount, totalCount, new java.util.Date(lastRefreshTime));
    }

    /**
     * 清空缓存
     */
    public void clearCache() {
        cache.clear();
        lastRefreshTime = 0;
        log.info("系统设置缓存已清空");
    }
}

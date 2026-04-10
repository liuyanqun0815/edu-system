package com.education.system.service;

import com.education.system.entity.SysSetting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 动态配置服务
 * 
 * 功能:
 * 1. 双源读取配置: sys_setting表 > Nacos配置 > 代码默认值
 * 2. 支持配置实时刷新
 * 3. 本地缓存提高性能
 * 
 * 优先级:
 * 1. sys_setting表: 业务配置,管理员后台可实时修改
 * 2. Nacos配置: 技术配置,环境相关
 * 3. 代码默认值: 兜底保障
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicConfigService {

    private final ISysSettingService settingService;

    /**
     * Nacos配置(通过@Value注入,作为第二优先级)
     */
    @Value("${edu.file.upload.path:doc/uploads/}")
    private String nacosUploadPath;

    @Value("${edu.file.upload.url-prefix:/uploads/}")
    private String nacosUrlPrefix;

    @Value("${edu.file.upload.max-size:10}")
    private int nacosMaxSize;

    @Value("${edu.file.upload.allowed-image-types:jpg,jpeg,png,gif,webp}")
    private String nacosAllowedImageTypes;

    /**
     * 本地缓存
     * key: group_code:setting_key
     * value: setting_value
     */
    private final Map<String, String> configCache = new ConcurrentHashMap<>();

    /**
     * 初始化: 加载所有配置到缓存
     */
    @PostConstruct
    public void init() {
        refreshCache();
        log.info("动态配置服务初始化完成,共加载 {} 条配置", configCache.size());
    }

    /**
     * 刷新缓存
     */
    public void refreshCache() {
        try {
            // 查询所有设置
            List<SysSetting> settings = settingService.listAll();
            if (settings == null || settings.isEmpty()) {
                log.debug("配置缓存刷新完成，无数据");
                return;
            }
            
            Map<String, String> newCache = settings.stream()
                .collect(Collectors.toMap(
                    s -> s.getGroupCode() + ":" + s.getSettingKey(),
                    s -> s.getSettingValue() != null ? s.getSettingValue() : "",
                    (v1, v2) -> v1
                ));
            
            configCache.clear();
            configCache.putAll(newCache);
            
            log.debug("配置缓存已刷新,当前 {} 条配置", configCache.size());
        } catch (Exception e) {
            log.error("刷新配置缓存失败", e);
        }
    }

    /**
     * 获取配置值(带缓存)
     * 
     * @param group 分组编码
     * @param key 配置键
     * @param nacosValue Nacos配置值(第二优先级)
     * @param defaultValue 默认值(第三优先级)
     * @return 配置值
     */
    public String getConfig(String group, String key, String nacosValue, String defaultValue) {
        // 第一优先级: sys_setting表
        String cacheKey = group + ":" + key;
        String dbValue = configCache.get(cacheKey);
        
        if (StringUtils.isNotBlank(dbValue)) {
            log.debug("从sys_setting读取配置: {}.{} = {}", group, key, dbValue);
            return dbValue;
        }
        
        // 第二优先级: Nacos配置
        if (StringUtils.isNotBlank(nacosValue)) {
            log.debug("从Nacos读取配置: {}.{} = {}", group, key, nacosValue);
            return nacosValue;
        }
        
        // 第三优先级: 代码默认值
        log.debug("使用默认配置: {}.{} = {}", group, key, defaultValue);
        return defaultValue;
    }

    /**
     * 获取字符串配置
     */
    public String getString(String group, String key, String defaultValue) {
        return getConfig(group, key, null, defaultValue);
    }

    /**
     * 获取整数配置
     */
    public int getInt(String group, String key, int defaultValue) {
        String value = getConfig(group, key, null, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("配置值转整数失败: {}.{}, value={}", group, key, value);
            return defaultValue;
        }
    }

    /**
     * 获取长整数配置
     */
    public long getLong(String group, String key, long defaultValue) {
        String value = getConfig(group, key, null, String.valueOf(defaultValue));
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.warn("配置值转长整数失败: {}.{}, value={}", group, key, value);
            return defaultValue;
        }
    }

    /**
     * 获取布尔配置
     */
    public boolean getBoolean(String group, String key, boolean defaultValue) {
        String value = getConfig(group, key, null, String.valueOf(defaultValue));
        return "1".equals(value) || "true".equalsIgnoreCase(value);
    }

    // =========================================
    // 文件上传配置(双源读取示例)
    // =========================================

    /**
     * 获取文件上传路径
     * 优先级: sys_setting(file:uploadPath) > Nacos > 默认值
     */
    public String getUploadPath() {
        return getConfig("file", "uploadPath", nacosUploadPath, "doc/uploads/");
    }

    /**
     * 获取文件访问URL前缀
     */
    public String getUrlPrefix() {
        return getConfig("file", "urlPrefix", nacosUrlPrefix, "/uploads/");
    }

    /**
     * 获取最大文件大小(MB)
     */
    public int getMaxSize() {
        String dbValue = configCache.get("file:maxSize");
        if (StringUtils.isNotBlank(dbValue)) {
            try {
                return Integer.parseInt(dbValue);
            } catch (NumberFormatException ignored) {}
        }
        return nacosMaxSize > 0 ? nacosMaxSize : 10;
    }

    /**
     * 获取允许的图片类型
     */
    public String[] getAllowedImageTypes() {
        String types = getConfig("file", "allowedImageTypes", nacosAllowedImageTypes, "jpg,jpeg,png,gif,webp");
        return types.split(",");
    }

    // =========================================
    // 安全配置读取
    // =========================================

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

    // =========================================
    // 学习业务配置读取
    // =========================================

    /**
     * 获取默认课程时长(秒)
     */
    public int getDefaultCourseDuration() {
        return getInt("learn", "defaultCourseDuration", 3600);
    }

    /**
     * 获取默认题目分值
     */
    public int getDefaultQuestionScore() {
        return getInt("learn", "defaultQuestionScore", 10);
    }

    /**
     * 获取考试及格分数线
     */
    public int getExamPassScore() {
        return getInt("learn", "examPassScore", 60);
    }

    // =========================================
    // 邮件配置读取
    // =========================================

    /**
     * 获取SMTP服务器地址
     */
    public String getSmtpHost() {
        return getString("notify", "smtpHost", "");
    }

    /**
     * 获取SMTP端口
     */
    public int getSmtpPort() {
        return getInt("notify", "smtpPort", 587);
    }

    /**
     * 获取发件人名称
     */
    public String getSenderName() {
        return getString("notify", "senderName", "智慧教培");
    }
}

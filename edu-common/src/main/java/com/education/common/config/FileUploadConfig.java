package com.education.common.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 文件上传配置
 * 
 * 配置读取优先级:
 * 1. Nacos配置(file.upload前缀): 环境相关配置
 * 2. 代码默认值: 兜底保障
 * 
 * 注意: 动态配置(数据库sys_setting表)由DynamicConfigService处理,
 *       该服务位于edu-system模块,通过事件机制刷新本配置。
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "edu.file.upload")
public class FileUploadConfig {
    
    /**
     * 文件存储路径
     */
    private String path = "doc/uploads/";
    
    /**
     * URL访问前缀
     */
    private String urlPrefix = "/uploads/";
    
    /**
     * 最大文件大小(MB)
     */
    private int maxSize = 10;
    
    /**
     * 允许的图片类型
     */
    private String[] allowedImageTypes = {"jpg", "jpeg", "png", "gif", "webp"};
    
    @PostConstruct
    public void init() {
        log.info("文件上传配置初始化: path={}, maxSize={}MB", path, maxSize);
    }
    
    /**
     * 获取上传路径
     */
    public String getPath() {
        return path;
    }
    
    /**
     * 获取访问URL前缀
     */
    public String getUrlPrefix() {
        return urlPrefix;
    }
    
    /**
     * 获取最大文件大小(MB)
     */
    public int getMaxSize() {
        return maxSize;
    }
    
    /**
     * 获取允许的图片类型
     */
    public String[] getAllowedImageTypes() {
        return allowedImageTypes;
    }
    
    /**
     * 检查文件类型是否允许
     */
    public boolean isAllowedImageType(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }
        String ext = extension.toLowerCase().replace(".", "");
        for (String allowed : allowedImageTypes) {
            if (allowed.equalsIgnoreCase(ext)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取最大文件大小(字节)
     */
    public long getMaxSizeBytes() {
        return (long) maxSize * 1024 * 1024;
    }
    
    /**
     * 动态更新配置(由DynamicConfigService调用)
     */
    public void updateConfig(String path, String urlPrefix, int maxSize, String[] allowedImageTypes) {
        if (path != null && !path.isEmpty()) {
            this.path = path;
        }
        if (urlPrefix != null && !urlPrefix.isEmpty()) {
            this.urlPrefix = urlPrefix;
        }
        if (maxSize > 0) {
            this.maxSize = maxSize;
        }
        if (allowedImageTypes != null && allowedImageTypes.length > 0) {
            this.allowedImageTypes = allowedImageTypes;
        }
        log.info("文件上传配置已动态更新: path={}, maxSize={}MB", this.path, this.maxSize);
    }
}

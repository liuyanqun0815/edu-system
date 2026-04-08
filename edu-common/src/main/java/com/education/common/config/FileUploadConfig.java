package com.education.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 文件上传配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadConfig {
    
    /**
     * 上传路径
     */
    private String path = "doc/uploads/";
    
    /**
     * 访问URL前缀
     */
    private String urlPrefix = "/uploads/";
    
    /**
     * 最大文件大小（MB）
     */
    private int maxSize = 10;
    
    /**
     * 允许的图片类型
     */
    private String[] allowedImageTypes = {"jpg", "jpeg", "png", "gif", "webp"};
}

package com.education.common.utils;

import com.education.common.config.FileUploadConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

/**
 * 文件上传工具类
 * 
 * <p>提供文件上传、删除等功能的统一封装，支持图片类型校验和大小限制。</p>
 * 
 * <h3>核心功能：</h3>
 * <ul>
 *   <li>图片上传：支持jpg/jpeg/png/gif/webp格式</li>
 *   <li>文件大小限制：可配置最大文件大小（默认10MB）</li>
 *   <li>目录结构：uploads/images/{yyyyMMdd}/{uuid}.{ext}</li>
 *   <li>自动创建上传目录</li>
 * </ul>
 * 
 * <h3>文件存储路径结构：</h3>
 * <pre>
 * uploads/
 * └── images/
 *     ├── 20260301/
 *     │   ├── a1b2c3d4e5f6.jpg
 *     │   └── b2c3d4e5f6g7.png
 *     └── 20260302/
 *         └── c3d4e5f6g7h8.jpg
 * </pre>
 * 
 * <h3>使用示例：</h3>
 * <pre>{@code
 * @Autowired
 * private FileUploadUtil fileUploadUtil;
 * 
 * @PostMapping("/upload")
 * public JsonResult<String> upload(@RequestParam("file") MultipartFile file) {
 *     String url = fileUploadUtil.uploadImage(file);
 *     return JsonResult.success("上传成功", url);
 * }
 * }</pre>
 * 
 * <h3>配置项（application.yml）：</h3>
 * <pre>
 * file:
 *   upload:
 *     path: uploads/           # 本地存储路径
 *     url-prefix: /uploads/    # 访问URL前缀
 *     max-size: 10             # 最大文件大小(MB)
 *     allowed-image-types: jpg,jpeg,png,gif,webp
 * </pre>
 * 
 * @see FileUploadConfig 文件上传配置类
 * @author education-team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileUploadUtil {

    private final FileUploadConfig fileUploadConfig;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @PostConstruct
    public void init() {
        // 确保上传目录存在
        try {
            Path uploadPath = Paths.get(fileUploadConfig.getPath());
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("创建上传目录: {}", uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("创建上传目录失败", e);
        }
    }

    /**
     * 上传图片
     *
     * @param file 图片文件
     * @return 访问URL
     */
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("文件名不能为空");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!isAllowedImageType(extension)) {
            throw new RuntimeException("不支持的图片格式，仅支持: " + String.join(", ", fileUploadConfig.getAllowedImageTypes()));
        }

        // 检查文件大小
        long maxSizeBytes = fileUploadConfig.getMaxSize() * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new RuntimeException("文件大小不能超过 " + fileUploadConfig.getMaxSize() + " MB");
        }

        try {
            // 生成文件路径：uploads/20260303/xxx.jpg
            String dateDir = LocalDateTime.now().format(DATE_FORMATTER);
            String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
            
            Path targetDir = Paths.get(fileUploadConfig.getPath(), "images", dateDir);
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            Path targetPath = targetDir.resolve(newFilename);
            Files.copy(file.getInputStream(), targetPath);

            // 返回访问URL
            String url = fileUploadConfig.getUrlPrefix() + "images/" + dateDir + "/" + newFilename;
            log.info("文件上传成功: {}", url);
            return url;

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : filename.substring(lastDotIndex + 1);
    }

    /**
     * 检查是否是允许的图片类型
     */
    private boolean isAllowedImageType(String extension) {
        return Arrays.stream(fileUploadConfig.getAllowedImageTypes())
                .anyMatch(type -> type.equalsIgnoreCase(extension));
    }

    /**
     * 删除文件
     *
     * @param url 文件URL
     */
    public void deleteFile(String url) {
        if (url == null || url.isEmpty()) {
            return;
        }

        try {
            String relativePath = url.replace(fileUploadConfig.getUrlPrefix(), "");
            Path filePath = Paths.get(fileUploadConfig.getPath(), relativePath);
            Files.deleteIfExists(filePath);
            log.info("文件删除成功: {}", filePath);
        } catch (IOException e) {
            log.error("文件删除失败: {}", url, e);
        }
    }
}

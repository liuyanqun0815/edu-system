package com.education.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * 题目图片处理工具类
 * 
 * <p>提供图片提取、压缩、存储和URL生成等功能</p>
 * 
 * @author education-team
 */
@Slf4j
public class QuestionImageUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    // 默认上传路径
    private static final String DEFAULT_UPLOAD_PATH = "doc/uploads/images/questions/";
    
    // 允许的图片格式
    private static final String[] ALLOWED_TYPES = {"jpg", "jpeg", "png", "gif", "webp", "bmp"};
    
    // 最大文件大小 5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    
    // 图片压缩质量
    private static final float COMPRESSION_QUALITY = 0.8f;

    /**
     * 从Word文档中提取图片并保存
     *
     * @param pictureDataList POI图片数据列表
     * @param questionId 题目ID
     * @return 图片URL列表
     */
    public static List<String> extractAndSaveImages(List<XWPFPictureData> pictureDataList, Long questionId) {
        List<String> imageUrls = new ArrayList<>();
        
        if (pictureDataList == null || pictureDataList.isEmpty()) {
            return imageUrls;
        }

        try {
            String dateDir = LocalDateTime.now().format(DATE_FORMATTER);
            String questionDir = "question_" + questionId;
            Path targetDir = Paths.get(DEFAULT_UPLOAD_PATH, dateDir, questionDir);
            
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            for (int i = 0; i < pictureDataList.size(); i++) {
                XWPFPictureData pictureData = pictureDataList.get(i);
                byte[] imageData = pictureData.getData();
                String fileName = pictureData.suggestFileExtension();
                
                // 验证图片格式
                if (!isAllowedImageType(fileName)) {
                    log.warn("不支持的图片格式: {}", fileName);
                    continue;
                }

                // 压缩图片
                byte[] compressedData = compressImage(imageData);
                
                // 生成文件名
                String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + fileName;
                Path targetPath = targetDir.resolve(newFilename);
                
                // 保存文件
                Files.write(targetPath, compressedData);
                
                // 生成URL
                String url = "/uploads/images/questions/" + dateDir + "/" + questionDir + "/" + newFilename;
                imageUrls.add(url);
                
                log.info("题目图片保存成功: questionId={}, url={}", questionId, url);
            }
        } catch (Exception e) {
            log.error("提取题目图片失败: questionId={}", questionId, e);
        }

        return imageUrls;
    }

    /**
     * 保存上传的题目图片
     *
     * @param file 图片文件
     * @param questionId 题目ID
     * @return 图片URL
     */
    public static String saveUploadedImage(MultipartFile file, Long questionId) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        // 验证文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("图片大小不能超过5MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("文件名不能为空");
        }

        String extension = getFileExtension(originalFilename);
        if (!isAllowedImageType(extension)) {
            throw new RuntimeException("不支持的图片格式，仅支持: " + String.join(", ", ALLOWED_TYPES));
        }

        try {
            String dateDir = LocalDateTime.now().format(DATE_FORMATTER);
            String questionDir = "question_" + questionId;
            Path targetDir = Paths.get(DEFAULT_UPLOAD_PATH, dateDir, questionDir);
            
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
            Path targetPath = targetDir.resolve(newFilename);
            
            // 读取并压缩图片
            byte[] imageData = file.getBytes();
            byte[] compressedData = compressImage(imageData);
            
            Files.write(targetPath, compressedData);
            
            String url = "/uploads/images/questions/" + dateDir + "/" + questionDir + "/" + newFilename;
            log.info("题目图片上传成功: questionId={}, url={}", questionId, url);
            return url;
            
        } catch (IOException e) {
            log.error("题目图片上传失败: questionId={}", questionId, e);
            throw new RuntimeException("图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 压缩图片
     *
     * @param imageData 原始图片数据
     * @return 压缩后的图片数据
     */
    public static byte[] compressImage(byte[] imageData) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
            BufferedImage originalImage = ImageIO.read(bais);
            
            if (originalImage == null) {
                // 无法读取为BufferedImage,返回原数据
                return imageData;
            }

            // 如果图片太大,进行缩放
            int maxWidth = 1920;
            int maxHeight = 1080;
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();

            if (width > maxWidth || height > maxHeight) {
                double scale = Math.min((double) maxWidth / width, (double) maxHeight / height);
                int newWidth = (int) (width * scale);
                int newHeight = (int) (height * scale);
                
                BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                resizedImage.getGraphics().drawImage(originalImage, 0, 0, newWidth, newHeight, null);
                
                // 写回字节数组
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                ImageIO.write(resizedImage, "jpg", baos);
                return baos.toByteArray();
            }

            return imageData;
        } catch (Exception e) {
            log.warn("图片压缩失败,使用原图", e);
            return imageData;
        }
    }

    /**
     * 将Base64图片转换为文件并保存
     *
     * @param base64Image Base64编码的图片
     * @param questionId 题目ID
     * @return 图片URL
     */
    public static String saveBase64Image(String base64Image, Long questionId) {
        if (base64Image == null || base64Image.isEmpty()) {
            throw new RuntimeException("Base64图片数据不能为空");
        }

        try {
            // 移除Base64前缀 (如: data:image/png;base64,)
            String base64Data = base64Image;
            String extension = "png"; // 默认格式
            
            if (base64Image.contains(",")) {
                String[] parts = base64Image.split(",", 2);
                String header = parts[0];
                base64Data = parts[1];
                
                // 从header中提取图片格式
                if (header.contains("image/jpeg") || header.contains("image/jpg")) {
                    extension = "jpg";
                } else if (header.contains("image/png")) {
                    extension = "png";
                } else if (header.contains("image/gif")) {
                    extension = "gif";
                } else if (header.contains("image/webp")) {
                    extension = "webp";
                }
            }

            // 解码Base64
            byte[] imageData = Base64.getDecoder().decode(base64Data);
            
            // 压缩图片
            byte[] compressedData = compressImage(imageData);

            // 保存文件
            String dateDir = LocalDateTime.now().format(DATE_FORMATTER);
            String questionDir = "question_" + questionId;
            Path targetDir = Paths.get(DEFAULT_UPLOAD_PATH, dateDir, questionDir);
            
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
            Path targetPath = targetDir.resolve(newFilename);
            Files.write(targetPath, compressedData);
            
            String url = "/uploads/images/questions/" + dateDir + "/" + questionDir + "/" + newFilename;
            log.info("Base64图片保存成功: questionId={}, url={}", questionId, url);
            return url;
            
        } catch (Exception e) {
            log.error("Base64图片保存失败: questionId={}", questionId, e);
            throw new RuntimeException("图片保存失败: " + e.getMessage());
        }
    }

    /**
     * 验证图片格式
     */
    private static boolean isAllowedImageType(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }
        String ext = extension.toLowerCase().replace(".", "");
        for (String allowed : ALLOWED_TYPES) {
            if (allowed.equals(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取文件扩展名
     */
    private static String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : filename.substring(lastDotIndex + 1);
    }

    /**
     * 获取图片的物理路径(用于导出时读取)
     *
     * @param url 图片URL
     * @return 物理路径
     */
    public static String getImagePhysicalPath(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        // 将URL转换为物理路径
        String relativePath = url.replace("/uploads/", "");
        return Paths.get("doc/uploads/", relativePath).toString();
    }
}

package org.xue.app.chat.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xue.app.chat.dto.VisionResponseDTO;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 图像处理服务
 * 负责图像文件的验证、处理和元数据提取
 */
@Service
@Slf4j
public class ImageProcessingService {
    
    // 支持的图像格式
    private static final List<String> SUPPORTED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp", "image/bmp"
    );
    
    // 最大文件大小：10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    /**
     * 验证图像文件是否有效
     */
    public boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("图像文件为空");
            return false;
        }
        
        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("图像文件过大: {} bytes", file.getSize());
            return false;
        }
        
        // 检查MIME类型
        String contentType = file.getContentType();
        if (contentType == null || !SUPPORTED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            log.warn("不支持的图像类型: {}", contentType);
            return false;
        }
        
        return true;
    }
    
    /**
     * 提取图像信息
     */
    public VisionResponseDTO.ImageInfo extractImageInfo(MultipartFile file) {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            
            return VisionResponseDTO.ImageInfo.builder()
                .fileName(file.getOriginalFilename())
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .width(image != null ? image.getWidth() : null)
                .height(image != null ? image.getHeight() : null)
                .build();
                
        } catch (IOException e) {
            log.error("读取图像信息失败", e);
            return VisionResponseDTO.ImageInfo.builder()
                .fileName(file.getOriginalFilename())
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .build();
        }
    }
    
    /**
     * 获取图像的Base64编码
     */
    public String getImageBase64(MultipartFile file) throws IOException {
        byte[] imageBytes = file.getBytes();
        return java.util.Base64.getEncoder().encodeToString(imageBytes);
    }
    
    /**
     * 构建图像数据URI
     */
    public String buildImageDataUri(MultipartFile file) throws IOException {
        String base64 = getImageBase64(file);
        String mimeType = file.getContentType();
        return String.format("data:%s;base64,%s", mimeType, base64);
    }
} 
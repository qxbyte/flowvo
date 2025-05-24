package org.xue.app.chat.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 图像识别响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisionResponseDTO {
    
    /**
     * 识别结果文本
     */
    private String content;
    
    /**
     * 使用的模型
     */
    private String model;
    
    /**
     * 处理的图片信息
     */
    private ImageInfo imageInfo;
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 错误信息（如果失败）
     */
    private String error;
    
    /**
     * 图片信息内嵌类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageInfo {
        private String fileName;
        private String mimeType;
        private Long fileSize;
        private Integer width;
        private Integer height;
    }
} 
package org.xue.app.chat.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 图像识别请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisionRequestDTO {
    
    /**
     * 对话ID（可选）
     */
    private String conversationId;
    
    /**
     * 用户输入的文本描述（可选）
     */
    private String message;
    
    /**
     * 使用的模型，默认 gpt-4o-mini
     */
    private String model;
    
    /**
     * 图片文件名
     */
    private String fileName;
    
    /**
     * 图片MIME类型
     */
    private String mimeType;
    
    /**
     * 图片大小（字节）
     */
    private Long fileSize;
} 
package org.xue.app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 文档上传请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadRequest {
    
    /**
     * 文档名称
     */
    private String name;
    
    /**
     * 文档内容（已解析的文本）
     */
    private String content;
    
    /**
     * 文档大小（字节）
     */
    private Long size;
    
    /**
     * 文档类型/MIME类型
     */
    private String type;
    
    /**
     * 文档标签
     */
    private List<String> tags;
    
    /**
     * 文档描述
     */
    private String description;
    
    /**
     * 文档来源路径
     */
    private String filePath;
    
    /**
     * 用户ID
     */
    private String userId;
} 
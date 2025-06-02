package org.xue.app.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文档实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {
    
    /**
     * 文档ID
     */
    private String id;
    
    /**
     * 文档名称
     */
    private String name;
    
    /**
     * 文档标签
     */
    private List<String> tags;
    
    /**
     * 文档大小（字节）
     */
    private Long size;
    
    /**
     * 文档类型/MIME类型
     */
    private String type;
    
    /**
     * 文档状态（PROCESSING, COMPLETED, FAILED）
     */
    private String status;
    
    /**
     * 原始文本内容
     */
    private String content;
    
    /**
     * 文档创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 文档修改时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 文档描述
     */
    private String description;
    
    /**
     * 文档来源路径
     */
    private String filePath;
    
    /**
     * 文档摘要
     */
    private String summary;
    
    /**
     * 向量化的文本块数量
     */
    private Integer chunkCount;
    
    /**
     * 用户ID
     */
    private String userId;
} 
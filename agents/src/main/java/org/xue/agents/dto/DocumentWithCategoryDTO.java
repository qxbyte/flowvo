package org.xue.agents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 包含分类信息的文档DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentWithCategoryDTO {
    
    /**
     * 文档ID
     */
    private String id;
    
    /**
     * 文档名称
     */
    private String name;
    
    /**
     * 文档内容
     */
    private String content;
    
    /**
     * 文件大小(字节)
     */
    private Long size;
    
    /**
     * 文档类型
     */
    private String type;
    
    /**
     * 标签列表
     */
    private List<String> tags;
    
    /**
     * 文档描述
     */
    private String description;
    
    /**
     * 文件路径
     */
    private String filePath;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 分类ID
     */
    private String categoryId;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 分类图标
     */
    private String categoryIcon;
    
    /**
     * 处理状态
     */
    private String status;
    
    /**
     * 块数量
     */
    private Integer chunkCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 
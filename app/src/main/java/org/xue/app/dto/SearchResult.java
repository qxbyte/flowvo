package org.xue.app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 搜索结果DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchResult {
    
    /**
     * 文档ID
     */
    private String documentId;
    
    /**
     * 文档名称
     */
    private String documentName;
    
    /**
     * 匹配的文本块
     */
    private String content;
    
    /**
     * 相似度得分
     */
    private Double score;
    
    /**
     * 文本块在原文档中的索引
     */
    private Integer chunkIndex;
} 
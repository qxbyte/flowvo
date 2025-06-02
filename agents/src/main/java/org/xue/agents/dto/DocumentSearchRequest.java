package org.xue.agents.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 文档搜索请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSearchRequest {
    
    /**
     * 搜索查询文本
     */
    private String query;
    
    /**
     * 搜索结果数量限制
     */
    private Integer limit = 10;
    
    /**
     * 相似度阈值（0-1之间）
     */
    private Double threshold = 0.5;
    
    /**
     * 用户ID（可选，用于过滤用户的文档）
     */
    private String userId;
} 
package org.xue.agents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识库问答请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeQaRequest {
    
    /**
     * 用户问题
     */
    private String question;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 会话ID（可选，用于上下文连续对话）
     */
    private String sessionId;
    
    /**
     * 期望的相关文档数量（默认5个）
     */
    @Builder.Default
    private Integer topK = 5;
    
    /**
     * 相似度阈值（默认0.7）
     */
    @Builder.Default
    private Double similarityThreshold = 0.7;
    
    /**
     * 指定文档分类（可选，不指定则搜索所有分类）
     */
    private String category;
    
    /**
     * 最大令牌数（默认2000）
     */
    @Builder.Default
    private Integer maxTokens = 2000;
    
    /**
     * 温度参数（默认0.1）
     */
    @Builder.Default
    private Double temperature = 0.1;
} 
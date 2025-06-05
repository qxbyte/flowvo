package org.xue.agents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识库问答响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeQaResponse {
    
    /**
     * 问答记录ID
     */
    private String id;
    
    /**
     * 用户问题
     */
    private String question;
    
    /**
     * AI回答内容
     */
    private String answer;
    
    /**
     * 信息来源列表
     */
    private List<SourceDocument> sources;
    
    /**
     * 问题分类
     */
    private String questionCategory;
    
    /**
     * 响应时间（毫秒）
     */
    private Integer responseTimeMs;
    
    /**
     * 向量相似度得分
     */
    private Double similarityScore;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 处理状态
     */
    private String status;
    
    /**
     * 来源文档信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceDocument {
        /**
         * 文档ID
         */
        private String documentId;
        
        /**
         * 文档标题
         */
        private String title;
        
        /**
         * 文档块内容
         */
        private String content;
        
        /**
         * 页码（如果有）
         */
        private Integer page;
        
        /**
         * 块编号
         */
        private Integer chunkIndex;
        
        /**
         * 相似度得分
         */
        private Double score;
    }
} 
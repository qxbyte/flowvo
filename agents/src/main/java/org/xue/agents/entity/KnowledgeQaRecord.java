package org.xue.agents.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识库问答记录实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "knowledge_qa_records")
public class KnowledgeQaRecord {
    
    /**
     * 问答记录ID (主键)
     */
    @Id
    private String id;
    
    /**
     * 用户ID
     */
    @Column(nullable = false)
    private String userId;
    
    /**
     * 用户问题
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;
    
    /**
     * AI回答内容
     */
    @Column(columnDefinition = "LONGTEXT")
    private String answer;
    
    /**
     * 信息来源（文档块信息）
     * 存储为JSON字符串
     */
    @Column(columnDefinition = "JSON")
    private String contextSources;
    
    /**
     * 问题分类（基于文档类型）
     */
    private String questionCategory;
    
    /**
     * 问题关键词（用于热门问题统计）
     */
    private String questionKeywords;
    
    /**
     * 响应时间（毫秒）
     */
    private Integer responseTimeMs;
    
    /**
     * 向量相似度得分
     */
    private Double similarityScore;
    
    /**
     * 用户反馈评分(1-5)
     */
    private Integer feedbackRating;
    
    /**
     * 用户反馈意见
     */
    private String feedbackComment;
    
    /**
     * 处理状态
     */
    @Enumerated(EnumType.STRING)
    private Status status;
    
    /**
     * 创建时间
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 处理状态枚举
     */
    public enum Status {
        PROCESSING,   // 处理中
        COMPLETED,    // 完成
        FAILED        // 失败
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = Status.PROCESSING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 
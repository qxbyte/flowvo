package org.xue.agents.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 热门问题统计实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "popular_questions",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_user_id_question_pattern_category", 
                           columnNames = {"userId", "questionPattern", "category"})
       },
       indexes = {
           @Index(name = "idx_user_id", columnList = "userId"),
           @Index(name = "idx_user_id_category", columnList = "userId,category"),
           @Index(name = "idx_user_id_trend_score", columnList = "userId,trendScore"),
           @Index(name = "idx_user_id_question_count", columnList = "userId,questionCount")
       })
public class PopularQuestion {
    
    /**
     * 统计ID (主键)
     */
    @Id
    private String id;
    
    /**
     * 用户ID (用于数据隔离)
     */
    @Column(nullable = false)
    private String userId;
    
    /**
     * 问题模式（经过标准化处理）
     */
    @Column(nullable = false)
    private String questionPattern;
    
    /**
     * 问题分类
     */
    private String category;
    
    /**
     * 问题出现次数
     */
    private Integer questionCount;
    
    /**
     * 最后提问时间
     */
    private LocalDateTime lastAskedTime;
    
    /**
     * 趋势得分（基于时间衰减和频次）
     */
    private BigDecimal trendScore;
    
    /**
     * 代表性问题（最完整的问题示例）
     */
    @Column(columnDefinition = "TEXT")
    private String representativeQuestion;
    
    /**
     * 标准答案（可选，用于快速回复）
     */
    @Column(columnDefinition = "LONGTEXT")
    private String standardAnswer;
    
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
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (questionCount == null) {
            questionCount = 1;
        }
        if (lastAskedTime == null) {
            lastAskedTime = LocalDateTime.now();
        }
        if (trendScore == null) {
            trendScore = BigDecimal.ZERO;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 
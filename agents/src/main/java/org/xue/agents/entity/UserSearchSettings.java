package org.xue.agents.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户搜索设置实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_search_settings")
public class UserSearchSettings {
    
    /**
     * 设置ID (主键)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户ID
     */
    @Column(nullable = false, unique = true)
    private String userId;
    
    /**
     * 检索数量 (Top-K)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer topK = 5;
    
    /**
     * 相似度阈值
     */
    @Column(nullable = false)
    @Builder.Default
    private Double similarityThreshold = 0.7;
    
    /**
     * 最大令牌数
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer maxTokens = 2000;
    
    /**
     * 温度参数
     */
    @Column(nullable = false)
    @Builder.Default
    private Double temperature = 0.1;
    
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
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 
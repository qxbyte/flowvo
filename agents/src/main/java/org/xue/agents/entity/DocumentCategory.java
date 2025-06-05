package org.xue.agents.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 文档分类实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "document_categories", 
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_user_id_name", columnNames = {"userId", "name"})
       },
       indexes = {
           @Index(name = "idx_user_id", columnList = "userId"),
           @Index(name = "idx_user_id_status", columnList = "userId,status"),
           @Index(name = "idx_user_id_sort_order", columnList = "userId,sortOrder")
       })
public class DocumentCategory {
    
    /**
     * 分类ID (主键)
     */
    @Id
    private String id;
    
    /**
     * 用户ID (用于数据隔离)
     */
    @Column(nullable = false)
    private String userId;
    
    /**
     * 分类名称
     */
    @Column(nullable = false)
    private String name;
    
    /**
     * 分类描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * 分类图标
     */
    private String icon;
    
    /**
     * 排序序号
     */
    private Integer sortOrder;
    
    /**
     * 状态
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
     * 状态枚举
     */
    public enum Status {
        ACTIVE,    // 激活
        INACTIVE   // 非激活
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
            status = Status.ACTIVE;
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 
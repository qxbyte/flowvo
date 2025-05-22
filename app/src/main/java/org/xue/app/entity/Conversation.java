package org.xue.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

/**
 * 对话实体类
 */
@Entity
@Table(name = "conversations")
@Data
public class Conversation {
    
    /**
     * 对话ID - UUID主键
     */
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;
    
    /**
     * 对话名称
     */
    @Column(name = "title", nullable = false)
    private String title;
    
    /**
     * 服务名称
     */
    @Column(name = "service", nullable = false)
    private String service;
    
    /**
     * 模型名称
     */
    @Column(name = "model")
    private String model;
    
    /**
     * 对话来源
     * chat: 普通聊天
     * business: 业务系统
     */
    @Column(name = "source", columnDefinition = "VARCHAR(20) DEFAULT 'chat'")
    private String source;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 创建之前自动设置时间
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        
        // 设置默认来源为chat
        if (source == null) {
            source = "chat";
        }
    }
    
    /**
     * 更新之前自动更新时间
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 
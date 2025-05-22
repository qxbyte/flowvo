package org.xue.app.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 */
@Entity
@Table(name = "chat_messages")
@Data
public class ChatMessage {
    
    /**
     * 消息ID - UUID主键
     */
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;
    
    /**
     * 关联的对话ID
     */
    @Column(name = "conversation_id", nullable = false)
    private String conversationId;
    
    /**
     * 消息角色：user、assistant、system、tool
     */
    @Column(name = "role", nullable = false)
    private String role;
    
    /**
     * 消息内容
     */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    // 新增字段：用于 assistant 消息保存 tool_calls
    @Column(name = "tool_calls", columnDefinition = "JSON")
    private String tool_calls;
    
    /**
     * 工具调用ID（如果有）
     */
    @Column(name = "tool_call_id")
    private String tool_call_id;
    
    /**
     * 工具名称（如果有）
     */
    @Column(name = "tool_name")
    private String toolName;
    
    /**
     * 消息序号
     */
    @Column(name = "sequence")
    private Integer sequence;
    
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
    }
    
    /**
     * 更新之前自动更新时间
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 
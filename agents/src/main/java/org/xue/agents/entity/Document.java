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
 * 文档实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "documents")
public class Document {
    
    /**
     * 文档ID (主键)
     */
    @Id
    private String id;
    
    /**
     * 文档名称
     */
    @Column(nullable = false)
    private String name;
    
    /**
     * 文档内容
     */
    @Column(columnDefinition = "TEXT")
    private String content;
    
    /**
     * 文件大小(字节)
     */
    private Long size;
    
    /**
     * 文档类型 (MIME类型)
     */
    private String type;
    
    /**
     * 标签列表，以逗号分隔存储
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "document_tags", joinColumns = @JoinColumn(name = "document_id"))
    @Column(name = "tag")
    @JsonProperty("tags")
    private List<String> tags;
    
    /**
     * 文档描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * 文件路径
     */
    private String filePath;
    
    /**
     * 用户ID
     */
    @Column(nullable = false)
    private String userId;
    
    /**
     * 文档分类ID
     */
    private String category;
    
    /**
     * 处理状态
     */
    @Enumerated(EnumType.STRING)
    private Status status;
    
    /**
     * 块数量
     */
    private Integer chunkCount;
    
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
     * 文档状态枚举
     */
    public enum Status {
        UPLOADING,    // 上传中
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
            status = Status.UPLOADING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 
package org.xue.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

/**
 * 文件附件实体类
 */
@Entity
@Table(name = "file_attachments")
@Data
public class FileAttachment {
    
    /**
     * 附件ID - UUID主键
     */
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;
    
    /**
     * 文件名
     */
    @Column(name = "file_name", nullable = false)
    private String fileName;
    
    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;
    
    /**
     * 文件类型（MIME类型）
     */
    @Column(name = "file_type", nullable = false)
    private String fileType;
    
    /**
     * 文件存储路径
     */
    @Column(name = "file_path", nullable = false)
    private String filePath;
    
    /**
     * 文件访问URL
     */
    @Column(name = "file_url")
    private String fileUrl;
    
    /**
     * 关联的对话ID
     */
    @Column(name = "conversation_id")
    private String conversationId;
    
    /**
     * 上传用户ID
     */
    @Column(name = "user_id", nullable = false)
    private String userId;
    
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
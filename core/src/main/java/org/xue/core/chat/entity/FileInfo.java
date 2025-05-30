package org.xue.core.chat.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "file_info")
public class FileInfo {
    @Id
    private String id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileExtension;

    @Column(name = "upload_time")
    private java.time.LocalDateTime uploadTime;
    
    @Column(name = "user_id")
    private Long userId;

    @PrePersist
    protected void onCreate() {
        uploadTime = java.time.LocalDateTime.now();
    }
}
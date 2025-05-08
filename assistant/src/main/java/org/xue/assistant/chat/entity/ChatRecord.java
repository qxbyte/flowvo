package org.xue.assistant.chat.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_record")
@Data
public class ChatRecord {
    @Id
    private String id;
    
    @Column(name = "user_id")
    private String userId;
    
    private String title;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    @Column
    private String type; // 对话类型：chat-普通对话，AIPROCESS-系统业务对话

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now().withNano((LocalDateTime.now().getNano() / 1_000_000) * 1_000_000);
        updateTime = LocalDateTime.now().withNano((LocalDateTime.now().getNano() / 1_000_000) * 1_000_000);
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now().withNano((LocalDateTime.now().getNano() / 1_000_000) * 1_000_000);
    }
}
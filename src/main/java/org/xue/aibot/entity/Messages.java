package org.xue.aibot.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;


@Entity
@Table(name = "messages")
@Data
public class Messages {
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    private ChatRecord chatRecord;

    private String role;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
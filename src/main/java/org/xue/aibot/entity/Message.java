package org.xue.aibot.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@NoArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long chatId;
    private String sender;
    @Column(name = "content", length = 5000)
    private String content;

    @Column(name = "created_at")
    private LocalTime createdAt = LocalTime.now();

    public Message(Long chatId, String sender, String content) {
        this.chatId = chatId;
        this.sender = sender;
        this.content = content;
    }

    public void setCreatedAt(LocalTime now) {
        this.createdAt = now;
    }
}

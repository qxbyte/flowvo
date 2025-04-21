package org.xue.aibot.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalTime;

@Entity
public class ChatRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Column(name = "created_at")
    private LocalTime createdAt = LocalTime.now();

    public Long getId() {
        return id;
    }

    public void setName(String s) {
        this.name = s;
    }

    public void setCreatedAt(LocalTime now) {
        this.createdAt = now;
    }
}

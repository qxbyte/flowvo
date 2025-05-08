package org.xue.assistant.functioncall.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Data;
import org.xue.assistant.functioncall.config.JsonNodeConverter;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "call_message")
public class CallMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id", nullable = false)
    private String chatId;

    @Column(nullable = false)
    private String role; // user / assistant / tool

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column
    private String name; // 函数名，仅 tool 用

    @Column(name = "tool_call_id")
    private String toolCallId; // 函数调用ID，仅 tool 用

    // 注意这里用 JsonNode 映射 JSON 字段（需要使用 Jackson + Hibernate Types）
    @Column(columnDefinition = "json")
    @Convert(converter = JsonNodeConverter.class)
    private JsonNode toolCalls;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}


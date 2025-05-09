package org.xue.assistant.functioncall.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.xue.assistant.functioncall.config.JsonNodeConverter;

import java.time.LocalDateTime;

@Entity
@Table(name = "call_message")
@Data
@NoArgsConstructor
public class CallMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
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
    @Column(name = "tool_calls", columnDefinition = "JSON")
    @Convert(converter = JsonNodeConverter.class)
    private JsonNode toolCalls;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}


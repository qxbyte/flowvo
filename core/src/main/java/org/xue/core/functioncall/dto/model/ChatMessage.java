package org.xue.core.functioncall.dto.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String role;
    private String tool_call_id;
    private String name;
    private String content;
    // 新增字段：用于 assistant 消息保存 tool_calls
    private JsonNode tool_calls;
    // 记录消息创建时间，使用JsonIgnore避免序列化到发送给大模型的JSON中
    @JsonIgnore
    private LocalDateTime createdAt;

    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public ChatMessage(String role, String tool_call_id, String content) {
        this.role = role;
        this.tool_call_id = tool_call_id;
        this.content = content;
    }
}


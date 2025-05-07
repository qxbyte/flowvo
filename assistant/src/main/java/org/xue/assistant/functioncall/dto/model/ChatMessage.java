package org.xue.assistant.functioncall.dto.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String role;
    private String tool_call_id;
    private String name;
    private String content;
    // ✅ 新增字段：用于 assistant 消息保存 tool_calls
    private JsonNode tool_calls;

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


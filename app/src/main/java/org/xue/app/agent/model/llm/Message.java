package org.xue.app.agent.model.llm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 大模型消息类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message {
    /**
     * 消息角色：system, user, assistant, tool
     */
    private String role;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 工具调用ID（仅tool角色消息使用）
     */
    private String tool_call_id;
    
    /**
     * 工具名称（仅tool角色消息使用）
     */
    private String name;

    /**
     * 调用工具详情
     */
    private JsonNode tool_calls;
    
    /**
     * 创建系统消息
     */
    public static Message systemMessage(String content) {
        return Message.builder()
                .role("system")
                .content(content)
                .build();
    }
    
    /**
     * 创建用户消息
     */
    public static Message userMessage(String content) {
        return Message.builder()
                .role("user")
                .content(content)
                .build();
    }
    
    /**
     * 创建助手消息
     */
    public static Message assistantMessage(String content, JsonNode tool_calls) {
        return Message.builder()
                .role("assistant")
                .content(content)
                .tool_calls(tool_calls)
                .build();
    }

    /**
     * 创建助手消息
     */
    public static Message assistantMessage(String content) {
        return Message.builder()
                .role("assistant")
                .content(content)
                .build();
    }
    
    /**
     * 创建工具消息
     */
    public static Message toolMessage(String toolCallId, String name, String content) {
        return Message.builder()
                .role("tool")
                .tool_call_id(toolCallId)
                .name(name)
                .content(content)
                .build();
    }
} 
package org.xue.agent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.xue.agent.model.llm.ToolCall;

import java.util.List;

/**
 * Agent响应模型类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponse {
    /**
     * 响应状态
     */
    private String status;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应内容
     */
    private String content;
    
    /**
     * 交互次数
     */
    private int interactions;
    
    /**
     * 总token数
     */
    private int totalTokens;
    
    /**
     * 工具调用列表
     */
    private List<ToolCall> toolCalls;
    
    /**
     * 创建成功响应
     */
    public static AgentResponse success(String content, int interactions, int totalTokens) {
        return AgentResponse.builder()
                .status("success")
                .content(content)
                .message(content)
                .interactions(interactions)
                .totalTokens(totalTokens)
                .build();
    }
    
    /**
     * 创建警告响应
     */
    public static AgentResponse warning(String message, int interactions, int totalTokens) {
        return AgentResponse.builder()
                .status("warning")
                .message(message)
                .content(message)
                .interactions(interactions)
                .totalTokens(totalTokens)
                .build();
    }
    
    /**
     * 创建错误响应
     */
    public static AgentResponse error(String message) {
        return AgentResponse.builder()
                .status("error")
                .message(message)
                .content(message)
                .build();
    }
} 
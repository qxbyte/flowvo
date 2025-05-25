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
     * 助手回复内容
     */
    private String assistantReply;
    
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
     * 获取助手回复内容
     * 如果assistantReply字段为空，则返回content字段的值
     */
    public String getAssistantReply() {
        return (assistantReply != null && !assistantReply.isEmpty()) ? 
                assistantReply : content;
    }
    
    /**
     * 创建成功响应
     */
    public static AgentResponse success(String content, int interactions, int totalTokens) {
        return AgentResponse.builder()
                .status("success")
                .content(content)
                .assistantReply(content)
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
                .assistantReply(message)
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
                .assistantReply(message)
                .build();
    }
} 
package org.xue.app.agents.model.llm;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 大模型助手消息类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssistantMessage {
    /**
     * 角色
     */
    private String role;
    
    /**
     * 内容
     */
    private String content;
    
    /**
     * 工具调用列表
     */
    private List<ToolCall> tool_calls;
    
    /**
     * 拒绝信息
     */
    private Object refusal;
    
    /**
     * 注释列表
     */
    private List<Object> annotations;
} 
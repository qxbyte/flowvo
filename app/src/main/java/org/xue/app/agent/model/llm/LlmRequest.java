package org.xue.app.agent.model.llm;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 大模型请求类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LlmRequest {
    /**
     * 模型名称
     */
    private String model;
    
    /**
     * 消息列表
     */
    private List<Message> messages;
    
    /**
     * 工具列表
     */
    private List<Tool> tools;
    
    /**
     * 工具选择方式
     */
    private String tool_choice;
    
    /**
     * 温度参数
     */
    private Double temperature;
    
    /**
     * 是否流式输出
     */
    private Boolean stream;
} 
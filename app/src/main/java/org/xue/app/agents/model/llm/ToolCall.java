package org.xue.app.agents.model.llm;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 大模型工具调用类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ToolCall {
    /**
     * 工具调用ID
     */
    private String id;
    
    /**
     * 工具类型
     */
    private String type;
    
    /**
     * 函数调用
     */
    private FunctionCall function;
} 
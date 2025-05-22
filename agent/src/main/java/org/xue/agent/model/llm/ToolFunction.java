package org.xue.agent.model.llm;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 大模型工具函数类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ToolFunction {
    /**
     * 函数名称
     */
    private String name;
    
    /**
     * 函数描述
     */
    private String description;
    
    /**
     * 函数参数
     */
    private ToolParameter parameters;
} 
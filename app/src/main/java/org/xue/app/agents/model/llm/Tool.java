package org.xue.app.agents.model.llm;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 大模型工具类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Tool {
    /**
     * 工具类型
     */
    private String type;
    
    /**
     * 工具函数
     */
    private ToolFunction function;
    
    /**
     * 创建函数类型工具
     */
    public static Tool functionTool(String name, String description, ToolParameter parameters) {
        return Tool.builder()
                .type("function")
                .function(ToolFunction.builder()
                        .name(name)
                        .description(description)
                        .parameters(parameters)
                        .build())
                .build();
    }
} 
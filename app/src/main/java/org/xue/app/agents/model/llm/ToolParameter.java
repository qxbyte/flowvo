package org.xue.app.agents.model.llm;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 大模型工具参数类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ToolParameter {
    /**
     * 参数类型
     */
    private String type;
    
    /**
     * 参数属性
     */
    private Map<String, ToolParameterProperty> properties;
    
    /**
     * 必需参数列表
     */
    private List<String> required;
} 
package org.xue.app.agent.model.llm;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 大模型工具参数属性类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ToolParameterProperty {
    /**
     * 属性类型
     */
    private String type;
    
    /**
     * 属性描述
     */
    private String description;
    
    /**
     * 数组项类型（仅当type为array时使用）
     */
    private ToolParameterItem items;
} 
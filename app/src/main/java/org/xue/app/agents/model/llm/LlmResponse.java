package org.xue.app.agents.model.llm;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 大模型响应类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LlmResponse {
    /**
     * 响应ID
     */
    private String id;
    
    /**
     * 对象类型
     */
    private String object;
    
    /**
     * 创建时间戳
     */
    private Long created;
    
    /**
     * 模型名称
     */
    private String model;
    
    /**
     * 选择结果列表
     */
    private List<Choice> choices;
    
    /**
     * 使用统计
     */
    private Usage usage;
    
    /**
     * 服务层级
     */
    private String service_tier;
    
    /**
     * 系统指纹
     */
    private String system_fingerprint;
} 
package org.xue.app.agents.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent请求模型类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentRequest {
    /**
     * 用户问题
     */
    private String query;
    
    /**
     * 服务名称
     */
    private String service;
    
    /**
     * 模型名称（可选）
     */
    private String model;
    
    /**
     * 温度参数（可选）
     */
    private Double temperature;
} 
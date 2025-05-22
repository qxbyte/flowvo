package org.xue.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent处理请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentProcessDTO {
    /**
     * 用户问题
     */
    private String query;
    
    /**
     * 服务名称
     */
    private String service;
    
    /**
     * 温度参数（可选）
     */
    private Double temperature;
} 
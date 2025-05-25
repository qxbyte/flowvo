package org.xue.app.service;

import org.xue.app.agents.model.AgentRequest;
import org.xue.app.agents.model.AgentResponse;

/**
 * 应用服务层Agent服务接口
 */
public interface AgentService {
    
    /**
     * 处理Agent请求
     *
     * @param request Agent请求
     * @return 处理结果
     */
    AgentResponse processAgentRequest(AgentRequest request);
} 
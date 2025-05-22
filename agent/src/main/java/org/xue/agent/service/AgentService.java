package org.xue.agent.service;

import org.xue.agent.model.AgentRequest;
import org.xue.agent.model.AgentResponse;

/**
 * Agent服务接口
 */
public interface AgentService {
    
    /**
     * 处理用户请求
     *
     * @param request 用户请求
     * @return 处理结果
     */
    AgentResponse process(AgentRequest request);
} 
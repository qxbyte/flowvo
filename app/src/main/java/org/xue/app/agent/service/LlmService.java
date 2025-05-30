package org.xue.app.agent.service;

import org.xue.app.agent.model.llm.LlmRequest;
import org.xue.app.agent.model.llm.LlmResponse;

/**
 * LLM服务接口
 */
public interface LlmService {
    
    /**
     * 调用LLM服务
     *
     * @param request LLM请求
     * @return LLM响应
     */
    LlmResponse callLlm(LlmRequest request);
} 
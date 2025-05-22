package org.xue.agent.service;

import org.xue.agent.model.llm.LlmRequest;
import org.xue.agent.model.llm.LlmResponse;

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
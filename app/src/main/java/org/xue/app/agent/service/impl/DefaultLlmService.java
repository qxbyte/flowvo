package org.xue.app.agent.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.xue.app.agent.config.AgentProperties;
import org.xue.app.agent.model.llm.LlmRequest;
import org.xue.app.agent.model.llm.LlmResponse;
import org.xue.app.agent.service.LlmService;

/**
 * 默认LLM服务实现
 */
@Service
public class DefaultLlmService implements LlmService {
    private static final Logger log = LoggerFactory.getLogger(DefaultLlmService.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private AgentProperties agentProperties;
    
    @Override
    public LlmResponse callLlm(LlmRequest request) {
        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(agentProperties.getLlmApi().getKey());
            
            // 创建HTTP实体
            HttpEntity<LlmRequest> entity = new HttpEntity<>(request, headers);
            
            // 发送请求
            ResponseEntity<LlmResponse> response = restTemplate.postForEntity(
                    agentProperties.getLlmApi().getUrl(),
                    entity,
                    LlmResponse.class
            );
            
            // 返回响应
            return response.getBody();
        } catch (Exception e) {
            log.error("调用LLM服务异常", e);
            throw new RuntimeException("调用LLM服务失败: " + e.getMessage(), e);
        }
    }
} 
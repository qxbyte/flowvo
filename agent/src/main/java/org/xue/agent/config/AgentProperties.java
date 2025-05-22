package org.xue.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Agent配置属性类
 */
@Data
@Component
@ConfigurationProperties(prefix = "agent")
public class AgentProperties {
    /**
     * 默认LLM模型
     */
    private String defaultModel = "gpt-4-turbo";
    
    /**
     * 是否启用Agent
     */
    private boolean enabled = true;
    
    /**
     * 最大交互次数
     */
    private int maxInteractions = 10;
    
    /**
     * 系统提示词
     */
    private String systemPrompt = "你是一个智能助手，需要得到用户想要的最终回复时才停止调用function，否则你需要持续响应function_call JSON与function API交互。";
    
    /**
     * 温度参数
     */
    private double temperature = 0.7;
    
    /**
     * App服务URL
     */
    private String appServiceUrl = "http://localhost:8080";
    
    /**
     * LLM API配置
     */
    private LlmApi llmApi = new LlmApi();
    
    /**
     * LLM API配置类
     */
    @Data
    public static class LlmApi {
        /**
         * API URL
         */
        private String url = "https://api.openai.com/v1/chat/completions";
        
        /**
         * API Key
         */
        private String key;
        
        /**
         * 超时设置（毫秒）
         */
        private int timeout = 60000;
    }
} 
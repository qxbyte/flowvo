package org.xue.agents.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 向量化配置类
 * 支持外部向量化服务和Spring AI内置服务的灵活切换
 */
@Configuration
@ConfigurationProperties(prefix = "embedding")
@Data
public class EmbeddingConfig {
    
    /**
     * 向量化服务类型
     * EXTERNAL: 使用外部Python向量化服务
     * SPRING_AI: 使用Spring AI内置的OpenAI向量化服务
     */
    private ServiceType type = ServiceType.SPRING_AI;
    
    /**
     * 外部向量化服务配置
     */
    private ExternalService external = new ExternalService();
    
    /**
     * 服务类型枚举
     */
    public enum ServiceType {
        EXTERNAL,    // 外部Python服务
        SPRING_AI    // Spring AI内置服务
    }
    
    /**
     * 外部服务配置
     */
    @Data
    public static class ExternalService {
        /**
         * 外部向量化服务URL
         */
        private String url = "http://localhost:8000";
        
        /**
         * 连接超时时间（毫秒）
         */
        private int connectTimeout = 5000;
        
        /**
         * 读取超时时间（毫秒）
         */
        private int readTimeout = 30000;
        
        /**
         * 文本切分配置
         */
        private int chunkSize = 300;
        private int chunkOverlap = 50;
    }
} 
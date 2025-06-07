package org.xue.agents.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 向量化配置类
 * 支持多种向量化服务的灵活切换：OpenAI、ZhiPuAI、外部服务
 */
@Configuration
@ConfigurationProperties(prefix = "embedding")
@Data
public class EmbeddingConfig {
    
    /**
     * 向量化服务类型
     * OPENAI: 使用OpenAI向量化服务 (text-embedding-ada-002, 1536维)
     * ZHIPUAI: 使用智谱AI向量化服务 (embedding-3, 2048维)
     * EXTERNAL: 使用外部Python向量化服务 (768维)
     */
    private ServiceType type = ServiceType.OPENAI;
    
    /**
     * 外部向量化服务配置
     */
    private ExternalService external = new ExternalService();
    
    /**
     * 服务类型枚举
     */
    public enum ServiceType {
        OPENAI,      // OpenAI服务 (1536维)
        ZHIPUAI,     // ZhiPuAI服务 (2048维)
        EXTERNAL     // 外部Python服务 (768维)
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
package org.xue.app.agents.client.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置类
 */
@Configuration
public class RestTemplateConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(RestTemplateConfig.class);
    
    @Bean
    public RestTemplate restTemplate() {
        logger.info("初始化RestTemplate");
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5秒连接超时
        factory.setReadTimeout(10000);   // 10秒读取超时
        
        return new RestTemplate(factory);
    }
} 
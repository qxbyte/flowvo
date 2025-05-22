package org.xue.app.milvus.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class MilvusRestTemplateConfig {
    @Bean("milvusRestTemplate")
    public RestTemplate milvusRestTemplate() {
        // milvus 模块的 RestTemplate 配置
        return new RestTemplate();
    }
}

package org.xue.assistant.functioncall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.xue.assistant.functioncall.client.OpenAiClient;

public class OpenAiClientConfig {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;
    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;
    @Bean
    public OpenAiClient openAiClient() {
        return new OpenAiClient(apiKey, baseUrl);
    }
}

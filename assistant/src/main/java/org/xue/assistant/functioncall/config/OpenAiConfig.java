package org.xue.assistant.functioncall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xue.assistant.functioncall.client.OpenAiClient;

@Configuration
public class OpenAiConfig {

    @Value("${ai.openai.api-key}")
    private String apiKey;
    @Value("${ai.openai.base-url}")
    private String baseUrl;
    @Bean
    public OpenAiClient openAiClient() {
        return new OpenAiClient(apiKey, baseUrl);
    }
}

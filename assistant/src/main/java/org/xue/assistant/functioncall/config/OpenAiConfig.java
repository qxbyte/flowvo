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
    @Value("${ai.openai.proxy.enabled:false}")
    private boolean proxyEnabled;
    @Value("${ai.openai.proxy.host:127.0.0.1}")
    private String proxyHost;
    @Value("${ai.openai.proxy.port:7890}")
    private int proxyPort;
    @Value("${ai.openai.connect-timeout:30}")
    private int connectTimeout;
    @Value("${ai.openai.read-timeout:60}")
    private int readTimeout;
    @Bean
    public OpenAiClient openAiClient() {
        return new OpenAiClient(apiKey, baseUrl, proxyEnabled, proxyHost, proxyPort, connectTimeout, readTimeout);
    }
}

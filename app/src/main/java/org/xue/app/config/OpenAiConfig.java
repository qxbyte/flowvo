package org.xue.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xue.app.client.OpenAiClient;

@Configuration
public class OpenAiConfig {

    // OpenAI配置
    @Value("${ai.openai.api-key}")
    private String openaiApiKey;
    @Value("${ai.openai.base-url}")
    private String openaiBaseUrl;
    @Value("${ai.openai.proxy.enabled:false}")
    private boolean openaiProxyEnabled;
    @Value("${ai.openai.proxy.host:127.0.0.1}")
    private String openaiProxyHost;
    @Value("${ai.openai.proxy.port:7890}")
    private int openaiProxyPort;
    @Value("${ai.openai.temperature:#{${ai.temperature:0.7}}}")
    private double openaiTemperature;
    @Value("${ai.openai.max-tokens:#{${ai.max-tokens:2048}}}")
    private int openaiMaxTokens;
    
    // DeepSeek配置
    @Value("${ai.deepseek.api-key}")
    private String deepseekApiKey;
    @Value("${ai.deepseek.base-url}")
    private String deepseekBaseUrl;
    @Value("${ai.deepseek.proxy.enabled:false}")
    private boolean deepseekProxyEnabled;
    @Value("${ai.deepseek.proxy.host:127.0.0.1}")
    private String deepseekProxyHost;
    @Value("${ai.deepseek.proxy.port:7890}")
    private int deepseekProxyPort;
    @Value("${ai.deepseek.temperature:#{${ai.temperature:0.8}}}")
    private double deepseekTemperature;
    @Value("${ai.deepseek.max-tokens:#{${ai.max-tokens:2048}}}")
    private int deepseekMaxTokens;
    
    // 通用配置 - 从ai根路径读取
    @Value("${ai.connect-timeout:30}")
    private int connectTimeout;
    @Value("${ai.read-timeout:120}")
    private int readTimeout;
    
    @Bean
    public OpenAiClient openAiClient() {
        return new OpenAiClient(
            // OpenAI配置
            openaiApiKey, openaiBaseUrl, openaiProxyEnabled, openaiProxyHost, openaiProxyPort,
            openaiTemperature, openaiMaxTokens,
            // DeepSeek配置  
            deepseekApiKey, deepseekBaseUrl, deepseekProxyEnabled, deepseekProxyHost, deepseekProxyPort,
            deepseekTemperature, deepseekMaxTokens,
            // 通用配置
            connectTimeout, readTimeout
        );
    }
}

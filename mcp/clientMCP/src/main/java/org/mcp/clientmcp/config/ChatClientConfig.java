package org.mcp.clientmcp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ChatClient配置类
 * 
 * 🎯 功能：
 * - 确保ChatClient Bean可用
 * - 支持Spring AI OpenAI自动配置
 * - 处理ChatClient依赖注入
 */
@Slf4j
@Configuration
@ConditionalOnClass(ChatClient.class)
public class ChatClientConfig {

    /**
     * 创建ChatClient Bean
     * 仅在没有现有ChatClient Bean时创建
     */
    @Bean
    @ConditionalOnMissingBean(ChatClient.class)
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        log.info("🔧 创建ChatClient Bean");
        return chatClientBuilder.build();
    }
} 
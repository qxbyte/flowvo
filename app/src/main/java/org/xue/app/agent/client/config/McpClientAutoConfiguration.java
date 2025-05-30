package org.xue.app.agent.client.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.xue.app.agent.client.core.ConnectionManager;
import org.xue.app.agent.client.core.McpClientTemplate;
import org.xue.app.agent.client.core.McpProperties;

/**
 * MCP客户端自动配置类
 * 当引入此jar包后会自动创建MCP客户端连接
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(McpProperties.class)
@Import({RestTemplateConfig.class})
@ConditionalOnProperty(prefix = "mcp", name = "enabled", havingValue = "true", matchIfMissing = true)
public class McpClientAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(McpClientAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public ConnectionManager connectionManager(
            McpProperties mcpProperties,
            RestTemplate restTemplate,
            Environment environment) {
        logger.info("初始化MCP连接管理器");
        return new ConnectionManager(mcpProperties, restTemplate, environment);
    }

    @Bean
    @ConditionalOnMissingBean
    public McpClientTemplate mcpClientTemplate(ConnectionManager connectionManager) {
        logger.info("初始化MCP客户端模板");
        return new McpClientTemplate(connectionManager);
    }
} 
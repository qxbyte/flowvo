package org.xue.mcp_client.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * MCP配置属性类
 */
@Data
@ConfigurationProperties(prefix = "mcp")
public class McpProperties {

    /**
     * MCP服务配置映射，key为服务名称，如mysql、redis等
     */
    private Map<String, ServerConfig> servers = new HashMap<>();

    /**
     * 是否启用MCP客户端
     */
    private boolean enabled = true;

    /**
     * 单个MCP服务配置
     */
    @Data
    public static class ServerConfig {
        /**
         * 服务URL
         */
        private String url;

        /**
         * 重试配置
         */
        private RetryConfig retry = new RetryConfig();

        /**
         * 连接超时（毫秒）
         */
        private int connectTimeout = 5000;

        /**
         * 读取超时（毫秒）
         */
        private int readTimeout = 10000;

        /**
         * 服务类型，默认为通用类型
         */
        private String type = "generic";
    }

    /**
     * 重试配置
     */
    @Data
    public static class RetryConfig {
        /**
         * 是否启用重试
         */
        private boolean enabled = true;

        /**
         * 重试间隔（毫秒）
         */
        private long interval = 10000;

        /**
         * 最大重试次数，-1表示无限重试
         */
        private int maxRetries = -1;
    }
} 
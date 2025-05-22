package org.xue.agent.client.core;

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
     * 心跳配置
     */
    private HeartbeatConfig heartbeat = new HeartbeatConfig();
    
    /**
     * 心跳配置
     */
    @Data
    public static class HeartbeatConfig {
        /**
         * 心跳间隔（毫秒）
         */
        private long interval = 10000;
    }

    /**
     * 单个MCP服务配置
     */
    @Data
    public static class ServerConfig {
        /**
         * 服务名称，用于API路径拼接
         * 如果不设置，将使用服务标识符
         */
        private String name;
        
        /**
         * 服务URL - 兼容旧配置
         */
        private String url;
        
        /**
         * 服务主机名或IP地址
         */
        private String host = "localhost";
        
        /**
         * 服务端口
         */
        private int port = 8080;
        
        /**
         * 是否为远程调用
         * true表示使用远程调用，使用配置的host和port
         * false表示使用本地调用，使用localhost和当前应用的端口
         */
        private boolean remote = true;
        
        /**
         * 协议（http或https）
         */
        private String protocol = "http";

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
        
        /**
         * 获取完整的服务URL
         * 
         * @param localPort 本地服务端口，当remote=false时使用
         * @return 完整的服务URL
         */
        public String getFullUrl(int localPort) {
            // 如果明确设置了url，优先使用url
            if (url != null && !url.isEmpty()) {
                return url;
            }
            
            // 构建URL
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(protocol).append("://");
            
            if (remote) {
                // 远程模式：使用配置的host和port
                urlBuilder.append(host).append(":").append(port);
            } else {
                // 本地模式：使用localhost和当前应用端口
                urlBuilder.append("localhost").append(":").append(localPort);
            }
            
            return urlBuilder.toString();
        }
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
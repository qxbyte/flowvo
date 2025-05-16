package org.xue.milvus.core;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP连接管理器
 * 负责管理所有MCP服务连接，执行心跳检查和重连
 */
public class ConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

    /**
     * MCP属性配置
     */
    private final McpProperties mcpProperties;

    /**
     * REST模板
     */
    private final RestTemplate restTemplate;
    
    /**
     * Spring环境
     */
    private final Environment environment;

    /**
     * 服务连接映射
     */
    private final Map<String, McpServer> serverMap = new ConcurrentHashMap<>();
    
    /**
     * 本地应用端口
     */
    private int localPort = 8080;

    /**
     * 构造函数
     *
     * @param mcpProperties MCP属性配置
     * @param restTemplate RestTemplate实例
     * @param environment Spring环境
     */
    @Autowired
    public ConnectionManager(McpProperties mcpProperties, RestTemplate restTemplate, Environment environment) {
        this.mcpProperties = mcpProperties;
        this.restTemplate = restTemplate;
        this.environment = environment;
        
        // 尝试从环境中获取当前应用的端口
        String portValue = environment.getProperty("server.port");
        if (portValue != null && !portValue.isEmpty()) {
            try {
                this.localPort = Integer.parseInt(portValue);
                logger.info("本地应用端口: {}", this.localPort);
            } catch (NumberFormatException e) {
                logger.warn("解析本地应用端口失败，使用默认端口8080: {}", e.getMessage());
            }
        } else {
            logger.info("未找到server.port配置，使用默认端口8080");
        }
    }

    /**
     * 配置REST模板
     */
    private void configureRestTemplate() {
        // 可以在这里设置REST模板的超时等配置
    }

    /**
     * 初始化连接
     */
    @PostConstruct
    public void init() {
        logger.info("初始化MCP服务连接...");
        
        // 遍历配置中的所有服务
        mcpProperties.getServers().forEach((name, config) -> {
            try {
                // 获取服务URL（考虑本地/远程模式）
                String serviceUrl = config.getFullUrl(localPort);
                
                if (serviceUrl != null && !serviceUrl.isEmpty()) {
                    McpServer server = new McpServer(name, config, serviceUrl, restTemplate);
                    boolean connected = server.init();
                    
                    serverMap.put(name, server);
                    
                    if (connected) {
                        if (config.isRemote()) {
                            logger.info("MCP服务 {} 远程连接成功: {}", name, serviceUrl);
                        } else {
                            logger.info("MCP服务 {} 本地连接成功: {}", name, serviceUrl);
                        }
                    } else {
                        logger.warn("MCP服务 {} 连接失败，将在后台尝试重连: {}", name, serviceUrl);
                    }
                } else {
                    logger.warn("MCP服务 {} 配置无效，URL为空", name);
                }
            } catch (Exception e) {
                logger.error("初始化MCP服务 {} 连接时发生错误: {}", name, e.getMessage(), e);
            }
        });
        
        if (serverMap.isEmpty()) {
            logger.warn("未配置任何MCP服务");
        } else {
            logger.info("已初始化 {} 个MCP服务连接", serverMap.size());
        }
    }

    /**
     * 定时心跳检查
     * 默认每10秒检查一次
     */
    @Scheduled(fixedDelayString = "${mcp.heartbeat.interval:10000}")
    public void heartbeatCheck() {
        serverMap.forEach((name, server) -> {
            McpProperties.ServerConfig config = server.getConfig();
            McpProperties.RetryConfig retry = config.getRetry();
            
            // 只有启用重试的服务才执行心跳检查
            if (retry.isEnabled()) {
                try {
                    boolean success = server.sendHeartbeat();
                    logger.info("❤️ MCP服务 {} 心跳 ==== "+System.currentTimeMillis(), name);
                    if (success && !server.isConnected()) {
                        logger.info("✅ MCP服务 {} 已恢复连接", name);
                    } else if (!success && server.isConnected()) {
                        logger.warn("🚫MCP服务 {} 连接已断开", name);
                    }
                } catch (Exception e) {
                    logger.debug("MCP服务 {} 心跳检查异常: {}", name, e.getMessage());
                }
            }
        });
    }

    /**
     * 获取指定名称的服务
     *
     * @param serverName 服务名称
     * @return 服务连接
     */
    public McpServer getServer(String serverName) {
        return serverMap.get(serverName);
    }

    /**
     * 获取所有服务连接
     *
     * @return 服务连接映射
     */
    public Map<String, McpServer> getAllServers() {
        return serverMap;
    }

    /**
     * 检查服务是否可用
     *
     * @param serverName 服务名称
     * @return 是否可用
     */
    public boolean isServerAvailable(String serverName) {
        McpServer server = serverMap.get(serverName);
        return server != null && server.isConnected();
    }
    
    /**
     * 获取本地端口
     * 
     * @return 本地应用端口
     */
    public int getLocalPort() {
        return localPort;
    }
} 
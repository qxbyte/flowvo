package org.xue.mcp_client.core;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.xue.mcp_client.model.JsonRpcRequest;
import org.xue.mcp_client.model.JsonRpcResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MCP服务器连接
 */
@Getter
public class McpServer {
    private static final Logger logger = LoggerFactory.getLogger(McpServer.class);

    /**
     * 服务名称
     */
    private final String name;

    /**
     * 服务配置
     */
    private final McpProperties.ServerConfig config;
    
    /**
     * 服务URL
     */
    private final String serviceUrl;

    /**
     * REST模板
     */
    private final RestTemplate restTemplate;

    /**
     * 连接状态
     */
    private boolean connected = false;

    /**
     * 上次心跳时间
     */
    private long lastHeartbeatTime = 0;

    /**
     * API模式描述
     */
    private Map<String, Object> apiSchema;

    /**
     * 连续失败次数
     */
    private final AtomicInteger failureCount = new AtomicInteger(0);

    /**
     * 构造函数
     *
     * @param name 服务名称
     * @param config 服务配置
     * @param serviceUrl 服务URL
     * @param restTemplate REST模板
     */
    public McpServer(String name, McpProperties.ServerConfig config, String serviceUrl, RestTemplate restTemplate) {
        this.name = name;
        this.config = config;
        this.serviceUrl = serviceUrl;
        this.restTemplate = restTemplate;
        
        logger.debug("创建MCP服务连接 {} -> {}", name, serviceUrl);
    }

    /**
     * 初始化连接
     *
     * @return 是否连接成功
     */
    public boolean init() {
        try {
            // 尝试获取API模式描述
            apiSchema = fetchApiSchema();
            
            // 尝试心跳
            boolean heartbeatSuccess = sendHeartbeat();
            
            if (heartbeatSuccess) {
                connected = true;
                failureCount.set(0);
                logger.info("成功连接到MCP服务: {}", name);
                return true;
            } else {
                connected = false;
                logger.warn("无法连接到MCP服务: {}", name);
                return false;
            }
        } catch (Exception e) {
            connected = false;
            logger.warn("连接MCP服务失败: {} - {}", name, e.getMessage());
            return false;
        }
    }

    /**
     * 获取API模式描述
     *
     * @return API模式描述
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchApiSchema() {
        try {
            String schemaUrl = getSchemaUrl();
            ResponseEntity<Map> response = restTemplate.getForEntity(schemaUrl, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.debug("成功获取MCP服务API模式: {}", name);
                return response.getBody();
            } else {
                logger.warn("获取MCP服务API模式失败: {} - 状态码: {}", name, response.getStatusCode());
                return new HashMap<>();
            }
        } catch (Exception e) {
            logger.warn("获取MCP服务API模式异常: {} - {}", name, e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * 发送心跳
     *
     * @return 心跳是否成功
     */
    public boolean sendHeartbeat() {
        try {
            JsonRpcRequest request = JsonRpcRequest.create("heartbeat", null);
            JsonRpcResponse response = executeRpc(request);
            
            lastHeartbeatTime = System.currentTimeMillis();
            
            if (response != null && response.getError() == null) {
                if (!connected) {
                    logger.info("MCP服务已恢复连接: {}", name);
                }
                connected = true;
                failureCount.set(0);
                return true;
            } else {
                handleConnectionFailure();
                return false;
            }
        } catch (Exception e) {
            logger.debug("心跳检查失败: {} - {}", name, e.getMessage());
            handleConnectionFailure();
            return false;
        }
    }

    /**
     * 处理连接失败
     */
    private void handleConnectionFailure() {
        int failures = failureCount.incrementAndGet();
        
        if (connected) {
            logger.warn("MCP服务连接中断: {} (连续失败次数: {})", name, failures);
            connected = false;
        }
    }

    /**
     * 执行RPC请求
     *
     * @param request RPC请求
     * @return RPC响应
     */
    public JsonRpcResponse executeRpc(JsonRpcRequest request) {
        try {
            String rpcUrl = getRpcUrl();
            ResponseEntity<JsonRpcResponse> response = restTemplate.postForEntity(
                    rpcUrl, 
                    request, 
                    JsonRpcResponse.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                logger.warn("RPC请求失败: {} - 状态码: {}", name, response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            logger.warn("RPC请求异常: {} - {}", name, e.getMessage());
            return null;
        }
    }

    /**
     * 获取Schema API URL
     * 
     * @return Schema API的完整URL
     */
    public String getSchemaUrl() {
        return serviceUrl + "/api/schema";
    }
    
    /**
     * 获取Schema API URL（带格式参数）
     * 
     * @param format API格式（如function_calling）
     * @return Schema API的完整URL（带格式参数）
     */
    public String getSchemaUrl(String format) {
        return getSchemaUrl() + "?format=" + format;
    }
    
    /**
     * 获取RPC API URL
     * 
     * @return RPC API的完整URL
     */
    public String getRpcUrl() {
        return serviceUrl + "/api/rpc";
    }
    
    /**
     * 获取服务名称
     * 优先使用配置中的name属性，如果没有则使用服务标识符
     * 
     * @return 服务名称
     */
    public String getServerName() {
        // 优先使用配置的name属性
        if (config.getName() != null && !config.getName().isEmpty()) {
            return config.getName();
        }
        // 回退使用服务标识符
        return name;
    }

    /**
     * 判断服务是否已连接
     */
    public boolean isConnected() {
        // 如果超过心跳间隔两倍的时间没有成功心跳，则认为连接已断开
        long heartbeatInterval = config.getRetry().getInterval();
        boolean heartbeatTimeout = System.currentTimeMillis() - lastHeartbeatTime > heartbeatInterval * 2;
        
        return connected && !heartbeatTimeout;
    }
} 
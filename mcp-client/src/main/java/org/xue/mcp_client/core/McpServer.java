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
     * @param restTemplate REST模板
     */
    public McpServer(String name, McpProperties.ServerConfig config, RestTemplate restTemplate) {
        this.name = name;
        this.config = config;
        this.restTemplate = restTemplate;
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
            String schemaUrl = config.getUrl() + "/api/schema/db";
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
            String rpcUrl = config.getUrl() + "/api/rpc/db";
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
     * 是否已连接
     *
     * @return 是否已连接
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * 获取服务URL
     *
     * @return 服务URL
     */
    public String getServiceUrl() {
        return config.getUrl();
    }
} 
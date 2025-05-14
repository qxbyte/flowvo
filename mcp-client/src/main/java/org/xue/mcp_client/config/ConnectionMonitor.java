package org.xue.mcp_client.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.xue.mcp_client.rpc.MCPDatabaseService;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 连接监控组件
 * 定期检查MCP-MySQL服务连接状态，并在服务不可用时尝试重新连接
 */
@Component
@EnableScheduling
public class ConnectionMonitor {
    
    private static final Logger logger = LoggerFactory.getLogger(ConnectionMonitor.class);
    
    @Autowired
    private MCPDatabaseService mcpDatabaseService;
    
    @Value("${mcp.mysql.url}")
    private String mcpMysqlUrl;
    
    @Value("${mcp.mysql.retry.enabled:true}")
    private boolean retryEnabled;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private boolean isServiceAvailable = false;
    
    /**
     * 定期检查连接状态
     * 默认每10秒检查一次
     */
    @Scheduled(fixedDelayString = "${mcp.mysql.retry.interval:10000}")
    public void checkConnection() {
        if (!retryEnabled) {
            return;
        }
        
        if (isProxyInstance(mcpDatabaseService)) {
            try {
                // 首先尝试使用JSON-RPC心跳方法检查连接
                try {
                    Map<String, Object> heartbeatResult = mcpDatabaseService.heartbeat();
                    if (heartbeatResult.containsKey("error")) {
                        // 降级服务返回的结果，需要尝试REST API方式
                        logger.debug("JSON-RPC心跳检查失败，尝试使用REST API: {}", heartbeatResult.get("error"));
                        checkConnectionWithRestApi();
                    } else {
                        if (!isServiceAvailable) {
                            logger.info("MCP数据库服务已恢复连接 (通过JSON-RPC心跳)");
                            isServiceAvailable = true;
                        }
                    }
                } catch (Exception e) {
                    // JSON-RPC调用失败，尝试REST API
                    logger.debug("JSON-RPC心跳调用失败: {}，尝试REST API方式", e.getMessage());
                    checkConnectionWithRestApi();
                }
            } catch (Exception e) {
                logger.warn("MCP数据库服务连接检查失败: {}", e.getMessage());
                tryReconnect();
            }
        }
    }
    
    /**
     * 使用REST API检查连接状态
     */
    private void checkConnectionWithRestApi() {
        try {
            // 尝试使用REST API心跳端点
            String heartbeatUrl = mcpMysqlUrl + "/api/rpc/db/heartbeat";
            ResponseEntity<Map> response = restTemplate.getForEntity(heartbeatUrl, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                if (!isServiceAvailable) {
                    logger.info("MCP数据库服务已恢复连接 (通过REST API心跳)");
                    isServiceAvailable = true;
                }
                return;
            }
        } catch (Exception e) {
            logger.debug("REST API心跳检查失败: {}", e.getMessage());
            
            // 尝试使用元数据REST API
            try {
                String metadataUrl = mcpMysqlUrl + "/api/rpc/db/metadata";
                ResponseEntity<Map> response = restTemplate.getForEntity(metadataUrl, Map.class);
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    if (!isServiceAvailable) {
                        logger.info("MCP数据库服务已恢复连接 (通过REST API元数据)");
                        isServiceAvailable = true;
                    }
                    return;
                }
            } catch (Exception ex) {
                logger.debug("REST API元数据检查失败: {}", ex.getMessage());
            }
        }
        
        // 如果所有REST API检查都失败，尝试使用getDatabaseMetadata方法
        try {
            Map<String, Object> metadataResult = mcpDatabaseService.getDatabaseMetadata();
            if (metadataResult.containsKey("error")) {
                tryReconnect();
            } else {
                if (!isServiceAvailable) {
                    logger.info("MCP数据库服务已恢复连接 (通过JSON-RPC元数据)");
                    isServiceAvailable = true;
                }
            }
        } catch (Exception e) {
            logger.debug("JSON-RPC元数据检查失败: {}", e.getMessage());
            tryReconnect();
        }
    }
    
    /**
     * 尝试重新连接服务
     */
    private void tryReconnect() {
        if (isServiceAvailable) {
            logger.info("MCP数据库服务连接已断开，尝试重新连接");
            isServiceAvailable = false;
        }
        
        try {
            String serviceUrl = mcpMysqlUrl + "/api/rpc/db";
            
            // 设置HTTP Header
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Accept", "application/json");
            
            // 创建新的JsonRpcHttpClient
            JsonRpcHttpClient client = new JsonRpcHttpClient(
                    objectMapper,
                    new URL(serviceUrl),
                    headers);
            
            // 连接超时设置
            client.setConnectionTimeoutMillis(5000);
            client.setReadTimeoutMillis(10000);
            
            // 创建新的代理
            MCPDatabaseService newService = ProxyUtil.createClientProxy(
                    getClass().getClassLoader(),
                    MCPDatabaseService.class,
                    client);
            
            // 测试新连接
            boolean connected = false;
            
            // 首先尝试心跳方法
            try {
                Map<String, Object> heartbeatResult = newService.heartbeat();
                if (!heartbeatResult.containsKey("error")) {
                    connected = true;
                } else {
                    logger.debug("心跳检查返回错误: {}", heartbeatResult.get("error"));
                }
            } catch (Exception e) {
                logger.debug("尝试心跳方法失败: {}", e.getMessage());
                
                // 尝试使用REST API
                try {
                    String heartbeatUrl = mcpMysqlUrl + "/api/rpc/db/heartbeat";
                    ResponseEntity<Map> response = restTemplate.getForEntity(heartbeatUrl, Map.class);
                    
                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                        connected = true;
                        logger.debug("通过REST API心跳检查成功连接");
                    }
                } catch (Exception ex) {
                    logger.debug("REST API心跳检查失败: {}", ex.getMessage());
                    
                    // 备选方案：尝试获取数据库元数据
                    try {
                        Map<String, Object> metadata = newService.getDatabaseMetadata();
                        if (!metadata.containsKey("error")) {
                            connected = true;
                            logger.debug("通过JSON-RPC元数据检查成功连接");
                        } else {
                            logger.debug("元数据检查返回错误: {}", metadata.get("error"));
                        }
                    } catch (Exception exc) {
                        logger.debug("无法通过JSON-RPC连接到服务: {}", exc.getMessage());
                        
                        // 最后尝试REST API的元数据端点
                        try {
                            String metadataUrl = mcpMysqlUrl + "/api/rpc/db/metadata";
                            ResponseEntity<Map> metadataResponse = restTemplate.getForEntity(metadataUrl, Map.class);
                            
                            if (metadataResponse.getStatusCode().is2xxSuccessful() && metadataResponse.getBody() != null) {
                                connected = true;
                                logger.debug("通过REST API元数据检查成功连接");
                            } else {
                                throw new Exception("REST API元数据检查失败");
                            }
                        } catch (Exception restEx) {
                            logger.debug("无法通过REST API连接到服务: {}", restEx.getMessage());
                        }
                    }
                }
            }
            
            if (!connected) {
                throw new Exception("无法通过任何方式验证连接");
            }
            
            // 更新现有服务的代理
            updateServiceProxy(newService);
            
            logger.info("成功重新连接到MCP数据库服务");
            isServiceAvailable = true;
        } catch (Exception e) {
            logger.debug("重新连接MCP数据库服务失败: {}", e.getMessage());
        }
    }
    
    /**
     * 检查对象是否是代理实例
     */
    private boolean isProxyInstance(Object obj) {
        return Proxy.isProxyClass(obj.getClass());
    }
    
    /**
     * 更新服务代理
     * 注意：这是一个hack方法，使用反射来替换现有代理的InvocationHandler
     */
    private void updateServiceProxy(MCPDatabaseService newService) {
        try {
            // 获取代理对象的invocationHandler
            Field handlerField = Proxy.class.getDeclaredField("h");
            handlerField.setAccessible(true);
            
            // 获取新旧代理的invocationHandler
            Object newHandler = handlerField.get(newService);
            
            // 替换旧代理的handler
            handlerField.set(mcpDatabaseService, newHandler);
        } catch (Exception e) {
            logger.error("更新服务代理失败: {}", e.getMessage(), e);
        }
    }
} 
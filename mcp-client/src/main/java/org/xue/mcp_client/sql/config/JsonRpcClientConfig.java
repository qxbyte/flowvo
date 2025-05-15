package org.xue.mcp_client.sql.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.xue.mcp_client.sql.rpc.MCPDatabaseService;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON-RPC客户端配置
 */
@Configuration
public class JsonRpcClientConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonRpcClientConfig.class);

    @Value("${mcp.mysql.url}")
    private String mcpMysqlUrl;
    
    @Value("${mcp.mysql.retry.enabled:true}")
    private boolean retryEnabled;
    
    @Value("${mcp.mysql.retry.interval:10000}")
    private long retryInterval;
    
    /**
     * 配置ObjectMapper
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
    
    /**
     * 配置MCP-MySQL数据库服务的JSON-RPC客户端
     */
    @Bean
    public MCPDatabaseService mcpDatabaseService(ObjectMapper objectMapper) {
        String serviceUrl = mcpMysqlUrl + "/api/rpc/db";
        logger.info("初始化MCP客户端，服务端URL: {}", serviceUrl);
        
        try {
            // 设置HTTP Header
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Accept", "application/json");
            
            // 创建JsonRpcHttpClient
            JsonRpcHttpClient client = new JsonRpcHttpClient(
                    objectMapper,
                    new URL(serviceUrl),
                    headers);
            
            // 连接超时设置
            client.setConnectionTimeoutMillis(15000);  // 15秒
            client.setReadTimeoutMillis(30000);        // 30秒
            
            // 创建代理
            MCPDatabaseService service = ProxyUtil.createClientProxy(
                    getClass().getClassLoader(),
                    MCPDatabaseService.class,
                    client);
            
            logger.info("MCP客户端初始化完成");
            return service;
        } catch (Exception e) {
            logger.error("初始化MCP客户端失败: {}", e.getMessage(), e);
            if (retryEnabled) {
                logger.info("将使用降级服务");
                // 返回一个降级的代理实现
                return createFallbackProxy();
            } else {
                throw new RuntimeException("无法连接到MCP服务: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * 创建一个降级的代理实现
     * 所有方法都返回友好的错误信息，不会导致应用崩溃
     */
    private MCPDatabaseService createFallbackProxy() {
        return new MCPDatabaseService() {
            private final String ERROR_MSG = "MCP服务暂时不可用，请稍后再试";
            
            @Override
            public Map<String, Object> executeQuery(String sql, Map<String, Object> params) {
                logger.warn("尝试调用executeQuery，但服务不可用。SQL: {}", sql);
                Map<String, Object> result = new HashMap<>();
                result.put("error", ERROR_MSG);
                result.put("rows", new ArrayList<>());
                return result;
            }

            @Override
            public Map<String, Object> executeUpdate(String sql, Map<String, Object> params) {
                logger.warn("尝试调用executeUpdate，但服务不可用。SQL: {}", sql);
                Map<String, Object> result = new HashMap<>();
                result.put("error", ERROR_MSG);
                result.put("affected", 0);
                return result;
            }

            @Override
            public Map<String, Object> executeBatch(List<Map<String, Object>> statements) {
                logger.warn("尝试调用executeBatch，但服务不可用");
                Map<String, Object> result = new HashMap<>();
                result.put("error", ERROR_MSG);
                result.put("affected", 0);
                return result;
            }

            @Override
            public List<String> listTables() {
                logger.warn("尝试调用listTables，但服务不可用");
                return new ArrayList<>();
            }

            @Override
            public List<Map<String, Object>> getTableSchema(String tableName) {
                logger.warn("尝试调用getTableSchema，但服务不可用。表名: {}", tableName);
                return new ArrayList<>();
            }

            @Override
            public Map<String, Object> getDatabaseMetadata() {
                logger.warn("尝试调用getDatabaseMetadata，但服务不可用");
                Map<String, Object> result = new HashMap<>();
                result.put("error", ERROR_MSG);
                return result;
            }

            @Override
            public List<Map<String, Object>> getQueryMetadata(String sql) {
                logger.warn("尝试调用getQueryMetadata，但服务不可用。SQL: {}", sql);
                return new ArrayList<>();
            }
            
            @Override
            public Map<String, Object> heartbeat() {
                logger.warn("尝试调用heartbeat，但服务不可用");
                Map<String, Object> result = new HashMap<>();
                result.put("status", "error");
                result.put("error", ERROR_MSG);
                result.put("timestamp", System.currentTimeMillis());
                return result;
            }
        };
    }
} 
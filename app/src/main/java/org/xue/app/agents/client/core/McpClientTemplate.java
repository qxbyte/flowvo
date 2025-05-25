package org.xue.app.agents.client.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xue.app.agents.client.exception.McpClientException;
import org.xue.app.agents.client.exception.McpServerException;
import org.xue.app.agents.client.model.JsonRpcRequest;
import org.xue.app.agents.client.model.JsonRpcResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP客户端模板类
 * 提供主要的MCP操作API
 */
@Component
public class McpClientTemplate {
    private static final Logger logger = LoggerFactory.getLogger(McpClientTemplate.class);

    /**
     * 连接管理器
     */
    private final ConnectionManager connectionManager;

    /**
     * 构造函数
     *
     * @param connectionManager 连接管理器
     */
    public McpClientTemplate(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * 获取Schema API URL
     *
     * @param serverName 服务名称
     * @return Schema API的完整URL
     */
    public String getSchemaUrl(String serverName) {
        McpServer server = connectionManager.getServer(serverName);
        if (server == null) {
            throw new McpClientException("未找到服务: " + serverName);
        }
        return server.getSchemaUrl();
    }
    
    /**
     * 获取Schema API URL（带格式参数）
     *
     * @param serverName 服务名称
     * @param format API格式（如function_calling）
     * @return Schema API的完整URL（带格式参数）
     */
    public String getSchemaUrl(String serverName, String format) {
        McpServer server = connectionManager.getServer(serverName);
        if (server == null) {
            throw new McpClientException("未找到服务: " + serverName);
        }
        return server.getSchemaUrl(format);
    }
    
    /**
     * 获取RPC API URL
     *
     * @param serverName 服务名称
     * @return RPC API的完整URL
     */
    public String getRpcUrl(String serverName) {
        McpServer server = connectionManager.getServer(serverName);
        if (server == null) {
            throw new McpClientException("未找到服务: " + serverName);
        }
        return server.getRpcUrl();
    }

    /**
     * 执行SQL查询
     *
     * @param serverName 服务名称
     * @param sql SQL语句
     * @return 查询结果
     */
    public List<Map<String, Object>> query(String serverName, String sql) {
        return query(serverName, sql, Collections.emptyMap());
    }

    /**
     * 执行SQL查询（带参数）
     *
     * @param serverName 服务名称
     * @param sql SQL语句
     * @param params 查询参数
     * @return 查询结果
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> query(String serverName, String sql, Map<String, Object> params) {
        // 构造RPC请求参数
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("sql", sql);
        requestParams.put("params", params);
        
        // 执行RPC调用
        JsonRpcResponse response = executeRpc(serverName, "query", requestParams);
        
        if (response == null) {
            return Collections.emptyList();
        }
        
        if (response.getError() != null) {
            throw new McpServerException(
                    "MCP服务查询错误: " + response.getError().getMessage(), 
                    response.getError().getCode());
        }
        
        if (response.getResult() == null) {
            return Collections.emptyList();
        }
        
        Map<String, Object> result = (Map<String, Object>) response.getResult();
        return (List<Map<String, Object>>) result.get("rows");
    }

    /**
     * 执行SQL更新（INSERT/UPDATE/DELETE）
     *
     * @param serverName 服务名称
     * @param sql SQL更新语句
     * @return 影响的行数
     */
    public int update(String serverName, String sql) {
        return update(serverName, sql, Collections.emptyMap());
    }

    /**
     * 执行SQL更新（INSERT/UPDATE/DELETE）（带参数）
     *
     * @param serverName 服务名称
     * @param sql SQL更新语句
     * @param params 更新参数
     * @return 影响的行数
     */
    @SuppressWarnings("unchecked")
    public int update(String serverName, String sql, Map<String, Object> params) {
        // 构造RPC请求参数
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("sql", sql);
        requestParams.put("params", params);
        
        // 执行RPC调用
        JsonRpcResponse response = executeRpc(serverName, "update", requestParams);
        
        if (response == null) {
            return 0;
        }
        
        if (response.getError() != null) {
            throw new McpServerException(
                    "MCP服务更新错误: " + response.getError().getMessage(), 
                    response.getError().getCode());
        }
        
        if (response.getResult() == null) {
            return 0;
        }
        
        Map<String, Object> result = (Map<String, Object>) response.getResult();
        return ((Number) result.get("affected")).intValue();
    }

    /**
     * 批量执行SQL语句（在事务中）
     *
     * @param serverName 服务名称
     * @param sqlStatements SQL语句和参数列表
     * @return 总影响行数
     */
    @SuppressWarnings("unchecked")
    public int executeBatch(String serverName, List<Map<String, Object>> sqlStatements) {
        // 构造RPC请求参数
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("statements", sqlStatements);
        
        // 执行RPC调用
        JsonRpcResponse response = executeRpc(serverName, "batch", requestParams);
        
        if (response == null) {
            return 0;
        }
        
        if (response.getError() != null) {
            throw new McpServerException(
                    "MCP服务批处理错误: " + response.getError().getMessage(), 
                    response.getError().getCode());
        }
        
        if (response.getResult() == null) {
            return 0;
        }
        
        Map<String, Object> result = (Map<String, Object>) response.getResult();
        return ((Number) result.get("affected")).intValue();
    }

    /**
     * 获取所有表名
     *
     * @param serverName 服务名称
     * @return 表名列表
     */
    @SuppressWarnings("unchecked")
    public List<String> listTables(String serverName) {
        // 执行RPC调用
        JsonRpcResponse response = executeRpc(serverName, "listTables", null);
        
        if (response == null) {
            return Collections.emptyList();
        }
        
        if (response.getError() != null) {
            throw new McpServerException(
                    "MCP服务获取表名错误: " + response.getError().getMessage(), 
                    response.getError().getCode());
        }
        
        if (response.getResult() == null) {
            return Collections.emptyList();
        }
        
        return (List<String>) response.getResult();
    }

    /**
     * 获取表结构
     *
     * @param serverName 服务名称
     * @param tableName 表名
     * @return 表结构信息
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getTableSchema(String serverName, String tableName) {
        // 构造RPC请求参数
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("tableName", tableName);
        
        // 执行RPC调用
        JsonRpcResponse response = executeRpc(serverName, "getTableSchema", requestParams);
        
        if (response == null) {
            return Collections.emptyList();
        }
        
        if (response.getError() != null) {
            throw new McpServerException(
                    "MCP服务获取表结构错误: " + response.getError().getMessage(), 
                    response.getError().getCode());
        }
        
        if (response.getResult() == null) {
            return Collections.emptyList();
        }
        
        return (List<Map<String, Object>>) response.getResult();
    }

    /**
     * 获取数据库元数据
     *
     * @param serverName 服务名称
     * @return 数据库元数据
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getDatabaseMetadata(String serverName) {
        // 执行RPC调用
        JsonRpcResponse response = executeRpc(serverName, "getDatabaseMetadata", null);
        
        if (response == null) {
            return Collections.emptyMap();
        }
        
        if (response.getError() != null) {
            throw new McpServerException(
                    "MCP服务获取数据库元数据错误: " + response.getError().getMessage(), 
                    response.getError().getCode());
        }
        
        if (response.getResult() == null) {
            return Collections.emptyMap();
        }
        
        return (Map<String, Object>) response.getResult();
    }

    /**
     * 获取查询元数据
     *
     * @param serverName 服务名称
     * @param sql SQL查询语句
     * @return 查询元数据
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getQueryMetadata(String serverName, String sql) {
        // 构造RPC请求参数
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("sql", sql);
        
        // 执行RPC调用
        JsonRpcResponse response = executeRpc(serverName, "getQueryMetadata", requestParams);
        
        if (response == null) {
            return Collections.emptyList();
        }
        
        if (response.getError() != null) {
            throw new McpServerException(
                    "MCP服务获取查询元数据错误: " + response.getError().getMessage(), 
                    response.getError().getCode());
        }
        
        if (response.getResult() == null) {
            return Collections.emptyList();
        }
        
        return (List<Map<String, Object>>) response.getResult();
    }

    /**
     * 执行自定义RPC方法
     *
     * @param serverName 服务名称
     * @param method 方法名
     * @param params 参数
     * @return RPC响应
     */
    public JsonRpcResponse executeRpc(String serverName, String method, Object params) {
        // 获取服务连接
        McpServer server = connectionManager.getServer(serverName);
        
        if (server == null) {
            logger.error("未找到名为 {} 的MCP服务", serverName);
            throw new McpClientException("未找到名为 " + serverName + " 的MCP服务");
        }
        
        if (!server.isConnected()) {
            logger.error("MCP服务 {} 当前不可用", serverName);
            throw new McpClientException("MCP服务 " + serverName + " 当前不可用");
        }
        
        // 创建RPC请求
        JsonRpcRequest request = JsonRpcRequest.create(method, params);
        
        // 执行RPC调用
        return server.executeRpc(request);
    }

    /**
     * 检查服务是否可用
     *
     * @param serverName 服务名称
     * @return 服务是否可用
     */
    public boolean isServerAvailable(String serverName) {
        try {
            Map<String, Map<String, Object>> serversStatus = getServersStatus();
            if (!serversStatus.containsKey(serverName)) {
                logger.error("服务不存在: {}", serverName);
                return false;
            }
            
            Map<String, Object> serverStatus = serversStatus.get(serverName);
            return (Boolean) serverStatus.get("connected");
        } catch (Exception e) {
            logger.error("检查服务状态异常: {}", serverName, e);
            return false;
        }
    }

    /**
     * 获取所有服务状态
     *
     * @return 服务状态映射
     */
    public Map<String, Map<String, Object>> getServersStatus() {
        return connectionManager.getServersStatus();
    }
} 
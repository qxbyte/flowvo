package org.xue.mcp_client.sql.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xue.mcp_client.sql.rpc.MCPDatabaseService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP数据库服务包装类
 * 提供更方便的API来访问远程MCP-MySQL服务
 */
@Service
public class MCPDatabaseServiceWrapper {

    private final MCPDatabaseService mcpDatabaseService;
    
    @Autowired
    public MCPDatabaseServiceWrapper(MCPDatabaseService mcpDatabaseService) {
        this.mcpDatabaseService = mcpDatabaseService;
    }
    
    /**
     * 执行SQL查询
     * 
     * @param sql SQL查询语句
     * @return 查询结果集
     */
    public List<Map<String, Object>> query(String sql) {
        return query(sql, new HashMap<>());
    }
    
    /**
     * 执行SQL查询（带参数）
     * 
     * @param sql SQL查询语句
     * @param params 查询参数
     * @return 查询结果集
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> query(String sql, Map<String, Object> params) {
        Map<String, Object> result = mcpDatabaseService.executeQuery(sql, params);
        return (List<Map<String, Object>>) result.get("rows");
    }
    
    /**
     * 执行SQL更新（INSERT/UPDATE/DELETE）
     * 
     * @param sql SQL更新语句
     * @return 影响的行数
     */
    public int update(String sql) {
        return update(sql, new HashMap<>());
    }
    
    /**
     * 执行SQL更新（INSERT/UPDATE/DELETE）（带参数）
     * 
     * @param sql SQL更新语句
     * @param params 更新参数
     * @return 影响的行数
     */
    public int update(String sql, Map<String, Object> params) {
        Map<String, Object> result = mcpDatabaseService.executeUpdate(sql, params);
        return (int) result.get("affected");
    }
    
    /**
     * 批量执行SQL语句（在事务中）
     * 
     * @param sqlStatements SQL语句和参数列表
     * @return 总影响行数
     */
    public int executeBatch(List<Map<String, Object>> sqlStatements) {
        Map<String, Object> result = mcpDatabaseService.executeBatch(sqlStatements);
        return (int) result.get("affected");
    }
    
    /**
     * 获取所有表名
     * 
     * @return 表名列表
     */
    public List<String> listTables() {
        return mcpDatabaseService.listTables();
    }
    
    /**
     * 获取表结构
     * 
     * @param tableName 表名
     * @return 表结构信息
     */
    public List<Map<String, Object>> getTableSchema(String tableName) {
        return mcpDatabaseService.getTableSchema(tableName);
    }
    
    /**
     * 获取数据库元数据
     * 
     * @return 数据库元数据
     */
    public Map<String, Object> getDatabaseMetadata() {
        return mcpDatabaseService.getDatabaseMetadata();
    }
    
    /**
     * 获取查询元数据
     * 
     * @param sql SQL查询语句
     * @return 查询元数据
     */
    public List<Map<String, Object>> getQueryMetadata(String sql) {
        return mcpDatabaseService.getQueryMetadata(sql);
    }
} 
package org.xue.mcpclient.rpc;

import com.googlecode.jsonrpc4j.JsonRpcMethod;
import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;


import java.util.List;
import java.util.Map;

/**
 * MCP数据库服务接口
 * 通过JSON-RPC调用远程MySQL数据库服务
 */
@JsonRpcService("/api/rpc/db")
public interface MCPDatabaseService {
    
    /**
     * 执行SQL查询
     */
    @JsonRpcMethod("query")
    Map<String, Object> executeQuery(
            @JsonRpcParam("sql") String sql, 
            @JsonRpcParam("params") Map<String, Object> params);
    
    /**
     * 执行SQL更新（INSERT/UPDATE/DELETE）
     */
    @JsonRpcMethod("update")
    Map<String, Object> executeUpdate(
            @JsonRpcParam("sql") String sql,
            @JsonRpcParam("params") Map<String, Object> params);
    
    /**
     * 批量执行SQL语句（在事务中）
     */
    @JsonRpcMethod("batch")
    Map<String, Object> executeBatch(
            @JsonRpcParam("statements") List<Map<String, Object>> statements);
    
    /**
     * 获取所有表名
     */
    @JsonRpcMethod("listTables")
    List<String> listTables();
    
    /**
     * 获取表结构
     */
    @JsonRpcMethod("getTableSchema")
    List<Map<String, Object>> getTableSchema(
            @JsonRpcParam("tableName") String tableName);
    
    /**
     * 获取数据库元数据
     */
    @JsonRpcMethod("getDatabaseMetadata")
    Map<String, Object> getDatabaseMetadata();
    
    /**
     * 获取查询元数据
     */
    @JsonRpcMethod("getQueryMetadata")
    List<Map<String, Object>> getQueryMetadata(
            @JsonRpcParam("sql") String sql);
    
    /**
     * 心跳检查
     * 用于检查服务是否可用的轻量级方法
     */
    @JsonRpcMethod("heartbeat")
    Map<String, Object> heartbeat();
} 
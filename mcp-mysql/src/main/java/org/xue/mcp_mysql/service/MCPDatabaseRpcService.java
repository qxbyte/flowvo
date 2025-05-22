package org.xue.mcp_mysql.service;

import org.xue.mcp_mysql.annotation.FunctionCallable;
import org.xue.mcp_mysql.annotation.FunctionParam;

import java.util.List;
import java.util.Map;

/**
 * MCP数据库RPC服务接口
 * 定义所有通过API暴露的方法
 */
public interface MCPDatabaseRpcService {
    
    /**
     * 执行SQL查询
     */
    @FunctionCallable(description = "执行SQL查询语句")
    Map<String, Object> query(@FunctionParam(description = "SQL查询语句(其中的表名、表结构字段信息、数据库信息可以通过“获取所有表名”、“获取表结构”、“获取数据库元数据”方法获取，不要随意猜测)如：SELECT * FROM 表名 WHERE 字段 >or= :字段值") String sql, @FunctionParam(description = "命名参数映射，如：{\"minAge\": 18}")Map<String, Object> params);
    
    /**
     * 执行SQL更新（INSERT/UPDATE/DELETE）
     */
    @FunctionCallable(description = "执行SQL更新语句（INSERT/UPDATE/DELETE）")
    Map<String, Object> update(@FunctionParam(description = "SQL更新语句(其中的表名、表结构字段信息、数据库信息可以通过“获取所有表名”、“获取表结构”、“获取数据库元数据”方法获取，不要随意猜测)，如：INSERT INTO 表名 (字段1, 字段2) VALUES (:值1, :值2)") String sql, @FunctionParam(description = "命名参数映射，如：{\"name\": \"张三\", \"age\": 25}") Map<String, Object> params);
    
    /**
     * 批量执行SQL语句（在事务中）
     */
    @FunctionCallable(description = "批量执行SQL语句（在事务中）")
    Map<String, Object> batch(@FunctionParam(description = "SQL语句列表，每个语句包含sql和params(其中的表名、表结构字段信息、数据库信息可以通过“获取所有表名”、“获取表结构”、“获取数据库元数据”方法获取，不要随意猜测)") List<Map<String, Object>> statements);
    
    /**
     * 获取所有表名
     */
    @FunctionCallable(description = "获取所有表名")
    List<String> listTables();
    
    /**
     * 获取表结构
     */
    @FunctionCallable(description = "获取表结构")
    List<Map<String, Object>> getTableSchema(@FunctionParam(description = "表名，如：users") String tableName);
    
    /**
     * 获取数据库元数据
     */
    @FunctionCallable(description = "获取数据库元数据")
    Map<String, Object> getDatabaseMetadata();
    
    /**
     * 获取查询元数据
     */
    @FunctionCallable(description = "获取查询元数据（列信息）")
    List<Map<String, Object>> getQueryMetadata(@FunctionParam(description = "SQL查询语句，如：SELECT id, name FROM userss") String sql);
    
    /**
     * 心跳检查
     * 用于轻量级连接验证
     */
    @FunctionCallable(description = "服务心跳检查")
    Map<String, Object> heartbeat();

} 
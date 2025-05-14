package org.xue.mcpmysql.service;

import org.xue.mcpmysql.annotation.FunctionCallable;
import org.xue.mcpmysql.annotation.FunctionParam;

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
    Map<String, Object> executeQuery(@FunctionParam(description = "SQL查询语句，如：SELECT * FROM users WHERE age >or= :minAge") String sql, @FunctionParam(description = "命名参数映射，如：{\"minAge\": 18}")Map<String, Object> params);
    
    /**
     * 执行SQL更新（INSERT/UPDATE/DELETE）
     */
    @FunctionCallable(description = "执行SQL更新语句（INSERT/UPDATE/DELETE）")
    Map<String, Object> executeUpdate(@FunctionParam(description = "SQL更新语句，如：INSERT INTO users (name, age) VALUES (:name, :age)") String sql, @FunctionParam(description = "命名参数映射，如：{\"name\": \"张三\", \"age\": 25}") Map<String, Object> params);
    
    /**
     * 批量执行SQL语句（在事务中）
     */
    @FunctionCallable(description = "批量执行SQL语句（在事务中）")
    Map<String, Object> executeBatch(@FunctionParam(description = "SQL语句列表，每个语句包含sql和params") List<Map<String, Object>> statements);
    
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
    @FunctionCallable(description = "获取所有表名")
    Map<String, Object> heartbeat();

} 
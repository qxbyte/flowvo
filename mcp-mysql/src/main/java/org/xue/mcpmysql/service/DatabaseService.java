package org.xue.mcpmysql.service;

import org.xue.mcpmysql.model.SqlQueryResult;
import org.xue.mcpmysql.model.SqlUpdateResult;

import java.util.List;
import java.util.Map;

/**
 * 数据库服务接口
 */
public interface DatabaseService {
    
    /**
     * 执行SQL查询
     * 
     * @param sql SQL查询语句
     * @param params 查询参数
     * @return 查询结果
     */
    SqlQueryResult executeQuery(String sql, Map<String, Object> params);
    
    /**
     * 执行SQL更新（INSERT/UPDATE/DELETE）
     * 
     * @param sql SQL更新语句
     * @param params 更新参数
     * @return 更新结果
     */
    SqlUpdateResult executeUpdate(String sql, Map<String, Object> params);
    
    /**
     * 批量执行SQL语句（在事务中）
     * 
     * @param statements SQL语句和参数列表
     * @return 更新结果
     */
    SqlUpdateResult executeBatch(List<Map<String, Object>> statements);
    
    /**
     * 获取所有表名
     * 
     * @return 表名列表
     */
    List<String> listTables();
    
    /**
     * 获取表结构
     * 
     * @param tableName 表名
     * @return 表结构信息
     */
    List<Map<String, Object>> getTableSchema(String tableName);
    
    /**
     * 获取数据库元数据
     * 
     * @return 数据库元数据
     */
    Map<String, Object> getDatabaseMetadata();
    
    /**
     * 获取查询元数据
     * 
     * @param sql SQL查询语句
     * @return 查询元数据
     */
    List<Map<String, Object>> getQueryMetadata(String sql);
    
    /**
     * 心跳检查
     * 用于检测服务是否正常运行的轻量级方法
     * 
     * @return 包含状态信息的Map
     */
    Map<String, Object> heartbeat();
} 
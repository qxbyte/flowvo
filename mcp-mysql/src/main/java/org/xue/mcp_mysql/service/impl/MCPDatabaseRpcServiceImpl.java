package org.xue.mcp_mysql.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xue.mcp_mysql.model.SqlQueryResult;
import org.xue.mcp_mysql.model.SqlUpdateResult;
import org.xue.mcp_mysql.service.DatabaseService;
import org.xue.mcp_mysql.service.MCPDatabaseRpcService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON-RPC控制器
 * 实现RPC服务接口，供RpcController调用
 */
@Service
public class MCPDatabaseRpcServiceImpl implements MCPDatabaseRpcService {
    
    private static final Logger logger = LoggerFactory.getLogger(MCPDatabaseRpcServiceImpl.class);
    
    private final DatabaseService databaseService;
    
    @Autowired
    public MCPDatabaseRpcServiceImpl(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }
    
    /**
     * 执行SQL查询
     */
    @Override
    public Map<String, Object> query(String sql, Map<String, Object> params) {
        logger.debug("RPC调用: executeQuery, SQL: {}", sql);
        try {
            // 确保params不为null
            Map<String, Object> safeParams = params != null ? params : Collections.emptyMap();
            SqlQueryResult queryResult = databaseService.executeQuery(sql, safeParams);
            
            Map<String, Object> result = new HashMap<>();
            result.put("rows", queryResult.getRows());
            result.put("status", queryResult.getStatus());
            result.put("count", queryResult.getCount());
            if (queryResult.getError() != null) {
                result.put("error", queryResult.getError());
            }
            
            return result;
        } catch (Exception e) {
            logger.error("执行查询时出错: {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("error", e.getMessage());
            result.put("rows", Collections.emptyList());
            result.put("count", 0);
            return result;
        }
    }
    
    /**
     * 执行SQL更新（INSERT/UPDATE/DELETE）
     */
    @Override
    public Map<String, Object> update(String sql, Map<String, Object> params) {
        logger.debug("RPC调用: executeUpdate, SQL: {}", sql);
        try {
            // 确保params不为null
            Map<String, Object> safeParams = params != null ? params : Collections.emptyMap();
            SqlUpdateResult updateResult = databaseService.executeUpdate(sql, safeParams);
            
            Map<String, Object> result = new HashMap<>();
            result.put("affected", updateResult.getAffected());
            result.put("status", updateResult.getStatus());
            if (updateResult.getError() != null) {
                result.put("error", updateResult.getError());
            }
            
            return result;
        } catch (Exception e) {
            logger.error("执行更新时出错: {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("error", e.getMessage());
            result.put("affected", 0);
            return result;
        }
    }
    
    /**
     * 批量执行SQL语句（在事务中）
     */
    @Override
    public Map<String, Object> batch(List<Map<String, Object>> statements) {
        logger.debug("RPC调用: executeBatch, 语句数量: {}", statements != null ? statements.size() : 0);
        try {
            // 确保statements不为null
            List<Map<String, Object>> safeStatements = statements != null ? statements : Collections.emptyList();
            SqlUpdateResult batchResult = databaseService.executeBatch(safeStatements);
            
            Map<String, Object> result = new HashMap<>();
            result.put("affected", batchResult.getAffected());
            result.put("status", batchResult.getStatus());
            if (batchResult.getError() != null) {
                result.put("error", batchResult.getError());
            }
            
            return result;
        } catch (Exception e) {
            logger.error("执行批处理时出错: {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("error", e.getMessage());
            result.put("affected", 0);
            return result;
        }
    }
    
    /**
     * 获取所有表名
     */
    @Override
    public List<String> listTables() {
        logger.debug("RPC调用: listTables");
        try {
            return databaseService.listTables();
        } catch (Exception e) {
            logger.error("获取表名列表时出错: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取表结构
     */
    @Override
    public List<Map<String, Object>> getTableSchema(String tableName) {
        logger.debug("RPC调用: getTableSchema, 表名: {}", tableName);
        try {
            return databaseService.getTableSchema(tableName);
        } catch (Exception e) {
            logger.error("获取表结构时出错: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取数据库元数据
     */
    @Override
    public Map<String, Object> getDatabaseMetadata() {
        logger.debug("RPC调用: getDatabaseMetadata");
        try {
            return databaseService.getDatabaseMetadata();
        } catch (Exception e) {
            logger.error("获取数据库元数据时出错: {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("error", e.getMessage());
            return result;
        }
    }
    
    /**
     * 获取查询元数据
     */
    @Override
    public List<Map<String, Object>> getQueryMetadata(String sql) {
        logger.debug("RPC调用: getQueryMetadata, SQL: {}", sql);
        try {
            return databaseService.getQueryMetadata(sql);
        } catch (Exception e) {
            logger.error("获取查询元数据时出错: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 心跳检查
     * 用于轻量级连接验证
     */
    @Override
    public Map<String, Object> heartbeat() {
        logger.debug("RPC调用: heartbeat");
        try {
            return databaseService.heartbeat();
        } catch (Exception e) {
            logger.error("心跳检查出错: {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("error", e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            return result;
        }
    }
} 
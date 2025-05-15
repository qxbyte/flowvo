package org.xue.mcp_mysql.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xue.mcp_mysql.model.SqlQueryResult;
import org.xue.mcp_mysql.model.SqlUpdateResult;
import org.xue.mcp_mysql.service.DatabaseService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 面向大模型的SQL执行控制器
 * 提供友好的API供大模型执行SQL查询
 */
@RestController
@RequestMapping("/api/llm/sql")
public class LlmSqlController {
    
    private static final Logger logger = LoggerFactory.getLogger(LlmSqlController.class);
    
    private final DatabaseService databaseService;
    
    @Autowired
    public LlmSqlController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }
    
    /**
     * 执行SQL查询
     */
    @PostMapping("/query")
    public Map<String, Object> executeQuery(
            @RequestBody Map<String, Object> request) {
        String sql = (String) request.get("sql");
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) request.getOrDefault("params", Collections.emptyMap());
        
        logger.debug("LLM API调用: executeQuery, SQL: {}, 参数: {}", sql, params);
        
        if (sql == null || sql.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("error", "SQL查询语句不能为空");
            return error;
        }
        
        // 对SQL进行简单验证
        if (isUnsafeQuery(sql)) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("error", "SQL查询语句包含不安全操作");
            return error;
        }
        
        try {
            // 如果是SELECT语句，执行查询
            if (isSelectQuery(sql)) {
                SqlQueryResult result = databaseService.executeQuery(sql, params);
                Map<String, Object> response = new HashMap<>();
                response.put("rows", result.getRows());
                response.put("status", result.getStatus());
                response.put("count", result.getCount());
                if (result.getError() != null) {
                    response.put("error", result.getError());
                }
                return response;
            } else {
                // 否则执行更新
                SqlUpdateResult result = databaseService.executeUpdate(sql, params);
                Map<String, Object> response = new HashMap<>();
                response.put("affected", result.getAffected());
                response.put("status", result.getStatus());
                if (result.getError() != null) {
                    response.put("error", result.getError());
                }
                return response;
            }
        } catch (Exception e) {
            logger.error("SQL执行失败: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("error", e.getMessage());
            return error;
        }
    }
    
    /**
     * 验证SQL是否是SELECT查询
     */
    private boolean isSelectQuery(String sql) {
        return sql.trim().toLowerCase().startsWith("select");
    }
    
    /**
     * 检查SQL是否包含不安全操作
     * 这只是一个简单的检查，实际应用中应当使用更复杂的验证和权限控制
     */
    private boolean isUnsafeQuery(String sql) {
        String lowerSql = sql.toLowerCase();
        
        // 检查是否包含可能有风险的关键词
        String[] dangerousKeywords = {
                "drop database", "drop schema",
                "truncate database", "truncate schema",
                "alter user", "create user", "drop user",
                "grant all", "shutdown"
        };
        
        for (String keyword : dangerousKeywords) {
            if (lowerSql.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 验证SQL语法
     */
    @PostMapping("/validate")
    public Map<String, Object> validateSql(@RequestBody Map<String, Object> request) {
        String sql = (String) request.get("sql");
        logger.debug("LLM API调用: validateSql, SQL: {}", sql);
        
        Map<String, Object> response = new HashMap<>();
        
        if (sql == null || sql.trim().isEmpty()) {
            response.put("valid", false);
            response.put("error", "SQL语句不能为空");
            return response;
        }
        
        // 检查是否包含不安全操作
        if (isUnsafeQuery(sql)) {
            response.put("valid", false);
            response.put("error", "SQL语句包含不安全操作");
            return response;
        }
        
        try {
            // 尝试获取查询元数据来验证SQL语法
            List<Map<String, Object>> metadata = databaseService.getQueryMetadata(sql);
            response.put("valid", true);
            response.put("metadata", metadata);
        } catch (Exception e) {
            logger.error("SQL语法验证失败: {}", e.getMessage(), e);
            response.put("valid", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
} 
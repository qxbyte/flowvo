package org.xue.mcpmysql.arteam.controller;

import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.xue.mcpmysql.arteam.service.RpcDatabaseService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 为大模型提供SQL生成和验证功能的控制器
 */
@RestController("arteamLlmSqlController")
@RequestMapping("/api/llm/sql")
public class LlmSqlController {

    private final RpcDatabaseService rpcDatabaseService;
    private final JdbcTemplate jdbcTemplate;

    public LlmSqlController(RpcDatabaseService rpcDatabaseService, JdbcTemplate jdbcTemplate) {
        this.rpcDatabaseService = rpcDatabaseService;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 验证SQL语法是否正确
     */
    @GetMapping("/validate")
    public Map<String, Object> validateSql(@RequestParam String sql) {
        Map<String, Object> result = new HashMap<>();
        result.put("sql", sql);
        
        try {
            // 使用SQL解析器验证语法，不实际执行
            jdbcTemplate.execute("EXPLAIN " + sql);
            
            // 尝试获取查询元数据，进一步验证
            List<Map<String, Object>> metadata = rpcDatabaseService.getQueryMetadata(sql);
            
            result.put("valid", true);
            result.put("columns", metadata);
        } catch (Exception e) {
            result.put("valid", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 验证并执行查询SQL，返回有限的结果集
     */
    @PostMapping(value = "/execute-query", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> executeQuery(@RequestBody Map<String, Object> request) {
        String sql = (String) request.get("sql");
        Map<String, Object> params = request.containsKey("params") ? 
                (Map<String, Object>) request.get("params") : new HashMap<>();
        int limit = request.containsKey("limit") ? (int) request.get("limit") : 10;
        
        Map<String, Object> result = new HashMap<>();
        result.put("sql", sql);
        
        try {
            // 限制结果集大小
            String limitedSql = sql;
            if (!limitedSql.toLowerCase().contains(" limit ")) {
                limitedSql += " LIMIT " + limit;
            }
            
            // 执行查询
            Map<String, Object> queryResult = rpcDatabaseService.executeQuery(limitedSql, params);
            
            result.put("success", true);
            result.put("data", queryResult.get("rows"));
            
            // 获取元数据
            List<Map<String, Object>> metadata = rpcDatabaseService.getQueryMetadata(sql);
            result.put("columns", metadata);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 为自然语言查询提供建议的SQL
     */
    @PostMapping("/suggest")
    public Map<String, Object> suggestSql(@RequestBody Map<String, Object> request) {
        String naturalLanguage = (String) request.get("query");
        String tableName = request.containsKey("table") ? (String) request.get("table") : null;
        
        Map<String, Object> result = new HashMap<>();
        result.put("query", naturalLanguage);
        
        try {
            // 获取表结构信息
            List<Map<String, Object>> schema = null;
            if (tableName != null) {
                schema = rpcDatabaseService.getTableSchema(tableName);
                result.put("table", tableName);
                result.put("schema", schema);
            } else {
                // 如果没有指定表，获取所有表的名称
                List<String> tables = rpcDatabaseService.listTables();
                result.put("availableTables", tables);
            }
            
            // 根据自然语言和表结构，提供SQL示例
            // 注意：这里只是提供一个结构，实际的SQL生成需要大模型完成
            Map<String, Object> suggestion = new HashMap<>();
            suggestion.put("format", "给出的只是SQL模板，实际生成需要使用LLM");
            suggestion.put("templateInfo", "这里可以放置如何根据表结构和自然语言生成SQL的提示信息");
            
            // 简单的SQL模板示例
            if (schema != null) {
                StringBuilder exampleSql = new StringBuilder("SELECT ");
                
                // 最多添加5个字段
                int fieldCount = 0;
                for (Map<String, Object> field : schema) {
                    if (fieldCount > 0) exampleSql.append(", ");
                    exampleSql.append(field.get("Field"));
                    fieldCount++;
                    if (fieldCount >= 5) break;
                }
                
                exampleSql.append(" FROM ").append(tableName);
                exampleSql.append(" WHERE 1=1 -- 在这里添加条件");
                exampleSql.append(" LIMIT 10");
                
                suggestion.put("exampleSql", exampleSql.toString());
            }
            
            result.put("suggestion", suggestion);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
} 
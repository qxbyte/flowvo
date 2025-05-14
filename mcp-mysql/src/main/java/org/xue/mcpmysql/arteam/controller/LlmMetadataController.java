package org.xue.mcpmysql.arteam.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xue.mcpmysql.arteam.service.RpcDatabaseService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 向大模型暴露MCP服务元数据的控制器
 */
@RestController("arteamLlmMetadataController")
@RequestMapping("/api/llm")
public class LlmMetadataController {

    private final RpcDatabaseService rpcDatabaseService;

    public LlmMetadataController(RpcDatabaseService rpcDatabaseService) {
        this.rpcDatabaseService = rpcDatabaseService;
    }

    /**
     * 获取数据库服务能力清单
     */
    @GetMapping("/capabilities")
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        
        // 基本能力描述
        capabilities.put("service", "mcp-mysql");
        capabilities.put("version", "1.0");
        capabilities.put("description", "MySQL数据库MCP服务，支持SQL查询、更新和元数据操作");
        
        // API方法列表
        Map<String, Object> methods = new HashMap<>();
        
        Map<String, Object> queryMethod = new HashMap<>();
        queryMethod.put("description", "执行SQL查询语句");
        queryMethod.put("params", Map.of(
                "sql", "SQL查询语句，如：SELECT * FROM users WHERE age > :minAge",
                "params", "命名参数映射，如：{\"minAge\": 18}"
        ));
        methods.put("query", queryMethod);
        
        Map<String, Object> updateMethod = new HashMap<>();
        updateMethod.put("description", "执行SQL更新语句（INSERT/UPDATE/DELETE）");
        updateMethod.put("params", Map.of(
                "sql", "SQL更新语句，如：INSERT INTO users (name, age) VALUES (:name, :age)",
                "params", "命名参数映射，如：{\"name\": \"张三\", \"age\": 25}"
        ));
        methods.put("update", updateMethod);
        
        Map<String, Object> batchMethod = new HashMap<>();
        batchMethod.put("description", "批量执行SQL语句（在一个事务中）");
        batchMethod.put("params", Map.of(
                "statements", "SQL语句列表，每个语句包含sql和params，如：[{\"sql\":\"INSERT INTO...\", \"params\":{...}}]"
        ));
        methods.put("batch", batchMethod);
        
        Map<String, Object> listTablesMethod = new HashMap<>();
        listTablesMethod.put("description", "获取所有表名");
        listTablesMethod.put("params", "无");
        methods.put("listTables", listTablesMethod);
        
        Map<String, Object> getTableSchemaMethod = new HashMap<>();
        getTableSchemaMethod.put("description", "获取表结构");
        getTableSchemaMethod.put("params", Map.of(
                "tableName", "表名，如：users"
        ));
        methods.put("getTableSchema", getTableSchemaMethod);
        
        Map<String, Object> getDbMetadataMethod = new HashMap<>();
        getDbMetadataMethod.put("description", "获取数据库元数据");
        getDbMetadataMethod.put("params", "无");
        methods.put("getDatabaseMetadata", getDbMetadataMethod);
        
        Map<String, Object> getQueryMetadataMethod = new HashMap<>();
        getQueryMetadataMethod.put("description", "获取查询元数据（列信息）");
        getQueryMetadataMethod.put("params", Map.of(
                "sql", "SQL查询语句，如：SELECT id, name FROM users"
        ));
        methods.put("getQueryMetadata", getQueryMetadataMethod);
        
        capabilities.put("methods", methods);
        
        return capabilities;
    }
    
    /**
     * 获取数据库结构信息
     */
    @GetMapping("/schema")
    public Map<String, Object> getDatabaseSchema() {
        Map<String, Object> schema = new HashMap<>();
        
        try {
            // 获取数据库元数据
            Map<String, Object> dbMetadata = rpcDatabaseService.getDatabaseMetadata();
            schema.put("databaseInfo", dbMetadata);
            
            // 获取所有表
            List<String> tables = rpcDatabaseService.listTables();
            
            // 获取每个表的结构
            Map<String, List<Map<String, Object>>> tableSchemas = new HashMap<>();
            for (String table : tables) {
                List<Map<String, Object>> tableSchema = rpcDatabaseService.getTableSchema(table);
                tableSchemas.put(table, tableSchema);
            }
            
            schema.put("tables", tableSchemas);
            schema.put("success", true);
        } catch (Exception e) {
            schema.put("success", false);
            schema.put("error", e.getMessage());
        }
        
        return schema;
    }
    
    /**
     * 获取单个表的结构信息
     */
    @GetMapping("/table")
    public Map<String, Object> getTableInfo(@RequestParam String tableName) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Map<String, Object>> tableSchema = rpcDatabaseService.getTableSchema(tableName);
            result.put("tableName", tableName);
            result.put("schema", tableSchema);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "获取表结构失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取查询的元数据信息
     */
    @GetMapping("/query-metadata")
    public Map<String, Object> getQueryInfo(@RequestParam String sql) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Map<String, Object>> metadata = rpcDatabaseService.getQueryMetadata(sql);
            result.put("sql", sql);
            result.put("columns", metadata);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "获取查询元数据失败: " + e.getMessage());
        }
        
        return result;
    }
} 
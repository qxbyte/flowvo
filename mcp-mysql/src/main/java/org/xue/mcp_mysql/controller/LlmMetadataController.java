package org.xue.mcp_mysql.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xue.mcp_mysql.service.DatabaseService;

import java.util.*;

/**
 * 面向大模型的数据库元数据控制器
 * 提供友好的API供大模型查询数据库结构信息
 */
@RestController
@RequestMapping("/api/llm/metadata")
public class LlmMetadataController {
    
    private static final Logger logger = LoggerFactory.getLogger(LlmMetadataController.class);
    
    private final DatabaseService databaseService;
    
    @Autowired
    public LlmMetadataController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }
    
    /**
     * 获取所有表名
     */
    @GetMapping("/tables")
    public Map<String, Object> listTables() {
        logger.debug("LLM API调用: listTables");
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> tables = databaseService.listTables();
            response.put("tables", tables);
            response.put("count", tables.size());
            response.put("status", "success");
        } catch (Exception e) {
            logger.error("获取表名失败: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 获取表结构
     */
    @GetMapping("/schema")
    public Map<String, Object> getTableSchema(@RequestParam String tableName) {
        logger.debug("LLM API调用: getTableSchema, 表名: {}", tableName);
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Map<String, Object>> columns = databaseService.getTableSchema(tableName);
            response.put("tableName", tableName);
            response.put("columns", columns);
            response.put("count", columns.size());
            response.put("status", "success");
        } catch (Exception e) {
            logger.error("获取表结构失败: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 获取数据库元数据
     */
    @GetMapping("/database")
    public Map<String, Object> getDatabaseMetadata() {
        logger.debug("LLM API调用: getDatabaseMetadata");
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> metadata = databaseService.getDatabaseMetadata();
            response.putAll(metadata);
            response.put("status", "success");
        } catch (Exception e) {
            logger.error("获取数据库元数据失败: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 获取大模型工具能力定义
     * 返回符合大模型工具格式的能力定义
     */
    @GetMapping("/capabilities")
    public Map<String, Object> getCapabilities() {
        logger.debug("LLM API调用: getCapabilities");
        
        // 创建工具定义
        Map<String, Object> tool = new HashMap<>();
        tool.put("type", "function");
        tool.put("function", createFunctionDefinition());
        
        // 创建响应
        Map<String, Object> response = new HashMap<>();
        response.put("tools", Collections.singletonList(tool));
        response.put("status", "success");
        
        return response;
    }
    
    /**
     * 创建函数定义
     */
    private Map<String, Object> createFunctionDefinition() {
        Map<String, Object> function = new HashMap<>();
        function.put("name", "query_database");
        function.put("description", "通过SQL语句查询数据库");
        
        // 创建参数定义
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        
        // 创建必需属性
        List<String> required = new ArrayList<>();
        required.add("sql");
        parameters.put("required", required);
        
        // 创建属性定义
        Map<String, Object> properties = new HashMap<>();
        
        // SQL参数
        Map<String, Object> sqlParam = new HashMap<>();
        sqlParam.put("type", "string");
        sqlParam.put("description", "要执行的SQL查询语句");
        properties.put("sql", sqlParam);
        
        // 参数列表参数
        Map<String, Object> paramsParam = new HashMap<>();
        paramsParam.put("type", "object");
        paramsParam.put("description", "SQL查询的参数，键为参数名，值为参数值");
        properties.put("params", paramsParam);
        
        // 添加属性到参数
        parameters.put("properties", properties);
        
        // 添加参数到函数定义
        function.put("parameters", parameters);
        
        return function;
    }
} 
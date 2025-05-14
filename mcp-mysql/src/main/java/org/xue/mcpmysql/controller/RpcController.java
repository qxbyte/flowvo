package org.xue.mcpmysql.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xue.mcpmysql.service.impl.MCPDatabaseRpcServiceImpl;
import org.xue.mcpmysql.service.ExposureApiService;
import org.xue.mcpmysql.enums.ApiFormatType;

import java.util.HashMap;
import java.util.Map;

/**
 * JSON-RPC控制器
 * 使用Spring MVC处理JSON-RPC请求
 */
@RestController
@RequestMapping("${app.rpc.path:/api/rpc/db}")
public class RpcController {
    
    private static final Logger logger = LoggerFactory.getLogger(RpcController.class);
    
    private final MCPDatabaseRpcServiceImpl jsonRpcService;
    private final ObjectMapper objectMapper;
    private final ExposureApiService exposureApiService;
    
    @Autowired
    public RpcController(MCPDatabaseRpcServiceImpl jsonRpcService, ObjectMapper objectMapper, ExposureApiService exposureApiService) {
        this.jsonRpcService = jsonRpcService;
        this.objectMapper = objectMapper;
        this.exposureApiService = exposureApiService;
    }
    
    /**
     * 处理JSON-RPC POST请求
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> handleJsonRpc(@RequestBody(required = false) Map<String, Object> request) {
        logger.debug("接收到JSON-RPC请求: {}", request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("jsonrpc", "2.0");
        
        // 处理空请求
        if (request == null) {
            response.put("id", null);
            Map<String, Object> error = new HashMap<>();
            error.put("code", -32700);
            error.put("message", "无效的JSON请求");
            response.put("error", error);
            return response;
        }
        
        // 提取请求ID和方法
        Object id = request.get("id");
        String method = (String) request.get("method");
        Object params = request.get("params");
        
        response.put("id", id);
        
        if (method == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", -32600);
            error.put("message", "无效的请求，缺少method字段");
            response.put("error", error);
            return response;
        }
        
        try {
            // 根据方法名调用对应的服务方法
            Object result = callServiceMethod(method, params);
            response.put("result", result);
        } catch (Exception e) {
            logger.error("处理RPC请求时发生错误: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", -32603);
            error.put("message", "内部错误: " + e.getMessage());
            error.put("data", e.getClass().getName());
            response.put("error", error);
        }
        
        return response;
    }
    
    /**
     * 根据方法名调用服务方法
     */
    private Object callServiceMethod(String methodName, Object params) throws Exception {
        // 心跳检查方法
        if ("heartbeat".equals(methodName)) {
            return jsonRpcService.heartbeat();
        }
        
        // API Schema方法
        if ("getApiSchema".equals(methodName)) {
            ApiFormatType formatType = ApiFormatType.RPC_JSON;
            if (params instanceof Map) {
                Map<String, Object> paramsMap = (Map<String, Object>) params;
                String format = (String) paramsMap.get("format");
                if ("function_calling".equalsIgnoreCase(format)) {
                    formatType = ApiFormatType.FUNCTION_CALLING;
                }
            }
            
            return exposureApiService.getApiDescription(formatType);
        }
        
        // 数据库元数据方法
        if ("getDatabaseMetadata".equals(methodName)) {
            return jsonRpcService.getDatabaseMetadata();
        }
        
        // 列出表名方法
        if ("listTables".equals(methodName)) {
            return jsonRpcService.listTables();
        }
        
        // 查询元数据方法
        if ("getQueryMetadata".equals(methodName) && params instanceof Map) {
            Map<String, Object> paramsMap = (Map<String, Object>) params;
            String sql = (String) paramsMap.get("sql");
            if (sql != null) {
                return jsonRpcService.getQueryMetadata(sql);
            }
        }
        
        // 表结构方法
        if ("getTableSchema".equals(methodName) && params instanceof Map) {
            Map<String, Object> paramsMap = (Map<String, Object>) params;
            String tableName = (String) paramsMap.get("tableName");
            if (tableName != null) {
                return jsonRpcService.getTableSchema(tableName);
            }
        }
        
        // 查询方法
        if ("query".equals(methodName) && params instanceof Map) {
            Map<String, Object> paramsMap = (Map<String, Object>) params;
            String sql = (String) paramsMap.get("sql");
            @SuppressWarnings("unchecked")
            Map<String, Object> queryParams = (Map<String, Object>) paramsMap.get("params");
            if (sql != null) {
                return jsonRpcService.executeQuery(sql, queryParams);
            }
        }
        
        // 更新方法
        if ("update".equals(methodName) && params instanceof Map) {
            Map<String, Object> paramsMap = (Map<String, Object>) params;
            String sql = (String) paramsMap.get("sql");
            @SuppressWarnings("unchecked")
            Map<String, Object> updateParams = (Map<String, Object>) paramsMap.get("params");
            if (sql != null) {
                return jsonRpcService.executeUpdate(sql, updateParams);
            }
        }
        
        // 批量执行方法
        if ("batch".equals(methodName) && params instanceof Map) {
            Map<String, Object> paramsMap = (Map<String, Object>) params;
            @SuppressWarnings("unchecked")
            Object statements = paramsMap.get("statements");
            if (statements instanceof java.util.List) {
                return jsonRpcService.executeBatch((java.util.List<Map<String, Object>>) statements);
            }
        }
        
        // 如果没有找到匹配的方法
        throw new Exception("未知方法: " + methodName);
    }
} 
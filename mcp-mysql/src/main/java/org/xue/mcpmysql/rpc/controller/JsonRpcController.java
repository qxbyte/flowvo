package org.xue.mcpmysql.rpc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xue.mcpmysql.rpc.dto.JsonRpcRequest;
import org.xue.mcpmysql.rpc.dto.JsonRpcResponse;
import org.xue.mcpmysql.rpc.dto.SqlQueryParams;
import org.xue.mcpmysql.rpc.service.DatabaseService;

import java.util.List;

/**
 * Spring MVC
 * JSON-RPC控制器
 * 支持标准的JSON-RPC 2.0协议
 */
@RestController("rpcJsonRpcController")
@RequestMapping("/rpc")
public class JsonRpcController {

    private final DatabaseService databaseService;
    private final ObjectMapper objectMapper;

    @Autowired
    public JsonRpcController(DatabaseService databaseService, ObjectMapper objectMapper) {
        this.databaseService = databaseService;
        this.objectMapper = objectMapper;
    }

    /**
     * 处理JSON-RPC请求
     *
     * @param request JSON-RPC请求
     * @return JSON-RPC响应
     */
    @PostMapping(value = "/sql", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonRpcResponse handleRpcRequest(@RequestBody JsonRpcRequest request) {
        try {
            // 方法分发
            return switch (request.getMethod()) {
                case "executeQuery" -> handleExecuteQuery(request);
                case "executeUpdate" -> handleExecuteUpdate(request);
                case "executeBatch" -> handleExecuteBatch(request);
                case "listTables" -> handleListTables(request);
                case "getTableSchema" -> handleGetTableSchema(request);
                case "getDatabaseMetadata" -> handleGetDatabaseMetadata(request);
                case "getQueryMetadata" -> handleGetQueryMetadata(request);
                default -> JsonRpcResponse.error(request.getId(), -32601, "方法不存在: " + request.getMethod());
            };
        } catch (Exception e) {
            return JsonRpcResponse.error(request.getId(), -32603, "内部错误: " + e.getMessage());
        }
    }

    // 处理各种方法的实现

    private JsonRpcResponse handleExecuteQuery(JsonRpcRequest request) {
        try {
            SqlQueryParams params = objectMapper.convertValue(request.getParams(), SqlQueryParams.class);
            return JsonRpcResponse.success(request.getId(), databaseService.executeQuery(params));
        } catch (Exception e) {
            return JsonRpcResponse.error(request.getId(), -32602, "无效的参数: " + e.getMessage());
        }
    }

    private JsonRpcResponse handleExecuteUpdate(JsonRpcRequest request) {
        try {
            SqlQueryParams params = objectMapper.convertValue(request.getParams(), SqlQueryParams.class);
            return JsonRpcResponse.success(request.getId(), databaseService.executeUpdate(params));
        } catch (Exception e) {
            return JsonRpcResponse.error(request.getId(), -32602, "无效的参数: " + e.getMessage());
        }
    }

    private JsonRpcResponse handleExecuteBatch(JsonRpcRequest request) {
        try {
            List<SqlQueryParams> paramsList = objectMapper.convertValue(request.getParams(), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, SqlQueryParams.class));
            return JsonRpcResponse.success(request.getId(), databaseService.executeBatch(paramsList));
        } catch (Exception e) {
            return JsonRpcResponse.error(request.getId(), -32602, "无效的参数: " + e.getMessage());
        }
    }

    private JsonRpcResponse handleListTables(JsonRpcRequest request) {
        return JsonRpcResponse.success(request.getId(), databaseService.listTables());
    }

    private JsonRpcResponse handleGetTableSchema(JsonRpcRequest request) {
        try {
            String tableName = objectMapper.convertValue(request.getParams(), String.class);
            return JsonRpcResponse.success(request.getId(), databaseService.getTableSchema(tableName));
        } catch (Exception e) {
            return JsonRpcResponse.error(request.getId(), -32602, "无效的参数: " + e.getMessage());
        }
    }

    private JsonRpcResponse handleGetDatabaseMetadata(JsonRpcRequest request) {
        return JsonRpcResponse.success(request.getId(), databaseService.getDatabaseMetadata());
    }

    private JsonRpcResponse handleGetQueryMetadata(JsonRpcRequest request) {
        try {
            String sql = objectMapper.convertValue(request.getParams(), String.class);
            return JsonRpcResponse.success(request.getId(), databaseService.getQueryMetadata(sql));
        } catch (Exception e) {
            return JsonRpcResponse.error(request.getId(), -32602, "无效的参数: " + e.getMessage());
        }
    }
} 
package org.xue.mcpmysql.arteam.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.xue.mcpmysql.arteam.model.JsonRpcRequest;
import org.xue.mcpmysql.arteam.model.JsonRpcResponse;
import org.xue.mcpmysql.arteam.service.RpcDatabaseService;
import org.xue.mcpmysql.rpc.dto.SqlQueryParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 手动实现的MCP数据库控制器
 * 作为simple-json-rpc-server实现的备用方案
 */
@RestController("arteamMcpDatabaseController")
@RequestMapping("/api/mcp")
public class McpDatabaseController {

    private final RpcDatabaseService rpcDatabaseService;
    private final ObjectMapper objectMapper;

    public McpDatabaseController(RpcDatabaseService rpcDatabaseService, 
                                @Qualifier("arteamObjectMapper") ObjectMapper objectMapper) {
        this.rpcDatabaseService = rpcDatabaseService;
        this.objectMapper = objectMapper;
    }

    /**
     * 处理JSON-RPC请求
     */
    @PostMapping(value = "/db", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonRpcResponse handleRequest(@RequestBody JsonRpcRequest request) {
        Object result = null;
        try {
            switch (request.getMethod()) {
                case "query" -> {
                    Map<String, Object> params = objectMapper.convertValue(request.getParams(), Map.class);
                    String sql = (String) params.get("sql");
                    Map<String, Object> sqlParams = params.containsKey("params") ? 
                            (Map<String, Object>) params.get("params") : new HashMap<>();
                    result = rpcDatabaseService.executeQuery(sql, sqlParams);
                }
                case "update" -> {
                    Map<String, Object> params = objectMapper.convertValue(request.getParams(), Map.class);
                    String sql = (String) params.get("sql");
                    Map<String, Object> sqlParams = params.containsKey("params") ? 
                            (Map<String, Object>) params.get("params") : new HashMap<>();
                    result = rpcDatabaseService.executeUpdate(sql, sqlParams);
                }
                case "batch" -> {
                    Map<String, Object> params = objectMapper.convertValue(request.getParams(), Map.class);
                    List<Map<String, Object>> statements = (List<Map<String, Object>>) params.get("statements");
                    result = rpcDatabaseService.executeBatch(statements);
                }
                case "listTables" -> result = rpcDatabaseService.listTables();
                case "getTableSchema" -> {
                    Map<String, Object> params = objectMapper.convertValue(request.getParams(), Map.class);
                    String tableName = (String) params.get("tableName");
                    result = rpcDatabaseService.getTableSchema(tableName);
                }
                case "getDatabaseMetadata" -> result = rpcDatabaseService.getDatabaseMetadata();
                case "getQueryMetadata" -> {
                    Map<String, Object> params = objectMapper.convertValue(request.getParams(), Map.class);
                    String sql = (String) params.get("sql");
                    result = rpcDatabaseService.getQueryMetadata(sql);
                }
                default -> {
                    return JsonRpcResponse.error(-32601, "方法未找到: " + request.getMethod(), request.getId());
                }
            }
            return JsonRpcResponse.success(result, request.getId());
        } catch (Exception e) {
            return JsonRpcResponse.error(-32603, "内部错误: " + e.getMessage(), request.getId());
        }
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "mcp-mysql-manual");
        return status;
    }
} 
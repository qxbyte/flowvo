package org.xue.mcpmysql.arteam.controller;

import com.github.arteam.simplejsonrpc.server.JsonRpcServer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.xue.mcpmysql.arteam.service.RpcDatabaseService;

import java.io.IOException;

/**
 * Simple-JSON-RPC-Server控制器
 */
@RestController("arteamJsonRpcController")
@RequestMapping("/api/rpc")
public class JsonRpcController {

    private final JsonRpcServer jsonRpcServer;
    private final RpcDatabaseService rpcDatabaseService;

    public JsonRpcController(RpcDatabaseService rpcDatabaseService, 
                            @Qualifier("arteamJsonRpcServer") JsonRpcServer jsonRpcServer) {
        this.rpcDatabaseService = rpcDatabaseService;
        this.jsonRpcServer = jsonRpcServer;
    }

    /**
     * 处理JSON-RPC请求
     */
    @PostMapping(value = "/db", produces = MediaType.APPLICATION_JSON_VALUE)
    public String handleDatabaseRequest(@RequestBody String request) throws IOException {
        return jsonRpcServer.handle(request, rpcDatabaseService);
    }

    /**
     * 处理JSON-RPC批量请求
     */
    @PostMapping(value = "/db/batch", produces = MediaType.APPLICATION_JSON_VALUE)
    public String handleBatchRequest(@RequestBody String request) throws IOException {
        return jsonRpcServer.handle(request, rpcDatabaseService);
    }
    
    /**
     * 健康检查端点
     */
    @GetMapping("/health")
    public String health() {
        return "{\"status\":\"UP\",\"service\":\"mcp-mysql-jsonrpc\"}";
    }
} 
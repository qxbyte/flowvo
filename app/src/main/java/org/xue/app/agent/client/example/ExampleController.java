package org.xue.app.agent.client.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.xue.app.agent.client.core.McpClientTemplate;
import org.xue.app.agent.client.exception.McpClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP客户端示例控制器
 * 展示如何使用MCP客户端进行操作
 */
@RestController
@RequestMapping("/api/mcp/example")
public class ExampleController {

    @Autowired
    private McpClientTemplate mcpTemplate;
    
    @Autowired
    private RestTemplate restTemplate;

    /**
     * 获取服务状态
     *
     * @return 服务状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("servers", mcpTemplate.getServersStatus());
        return ResponseEntity.ok(result);
    }
    
    /**
     * 验证API模式描述接口
     * 
     * @param server 服务名称
     * @param format 格式类型（rpc_json或function_calling）
     * @return API模式描述
     */
    @GetMapping("/api-schema")
    public ResponseEntity<Map<String, Object>> getApiSchema(
            @RequestParam("server") String server,
            @RequestParam(value = "format", defaultValue = "rpc_json") String format) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取服务状态
            Map<String, Map<String, Object>> serversStatus = mcpTemplate.getServersStatus();
            if (!serversStatus.containsKey(server)) {
                result.put("status", "error");
                result.put("error", "未找到服务: " + server);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            }
            
            Map<String, Object> serverStatus = serversStatus.get(server);
            if (!(Boolean) serverStatus.get("connected")) {
                result.put("status", "error");
                result.put("error", "服务未连接: " + server);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(result);
            }
            
            // 构建API模式描述请求URL
            String serviceUrl = (String) serverStatus.get("url");
            String schemaUrl = serviceUrl + "/api/schema?format=" + format;
            
            // 发送请求获取API模式描述
            ResponseEntity<Map> response = restTemplate.getForEntity(schemaUrl, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                result.put("status", "ok");
                result.put("schema", response.getBody());
            } else {
                result.put("status", "error");
                result.put("error", "获取API模式描述失败: " + response.getStatusCode());
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("error", "请求异常: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 执行SQL查询
     *
     * @param server 服务名称
     * @param sql SQL语句
     * @return 查询结果
     */
    @GetMapping("/query")
    public ResponseEntity<Map<String, Object>> query(
            @RequestParam("server") String server,
            @RequestParam("sql") String sql) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Map<String, Object>> rows = mcpTemplate.query(server, sql);
            result.put("status", "ok");
            result.put("rows", rows);
            result.put("count", rows.size());
        } catch (McpClientException e) {
            result.put("status", "error");
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取数据库元数据
     *
     * @param server 服务名称
     * @return 数据库元数据
     */
    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata(
            @RequestParam("server") String server) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> metadata = mcpTemplate.getDatabaseMetadata(server);
            result.put("status", "ok");
            result.put("metadata", metadata);
        } catch (McpClientException e) {
            result.put("status", "error");
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取表列表
     *
     * @param server 服务名称
     * @return 表列表
     */
    @GetMapping("/tables")
    public ResponseEntity<Map<String, Object>> getTables(
            @RequestParam("server") String server) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<String> tables = mcpTemplate.listTables(server);
            result.put("status", "ok");
            result.put("tables", tables);
            result.put("count", tables.size());
        } catch (McpClientException e) {
            result.put("status", "error");
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取表结构
     *
     * @param server 服务名称
     * @param table 表名
     * @return 表结构
     */
    @GetMapping("/table-schema")
    public ResponseEntity<Map<String, Object>> getTableSchema(
            @RequestParam("server") String server,
            @RequestParam("table") String table) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Map<String, Object>> schema = mcpTemplate.getTableSchema(server, table);
            result.put("status", "ok");
            result.put("schema", schema);
            result.put("count", schema.size());
        } catch (McpClientException e) {
            result.put("status", "error");
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
}
# MCP Client

MCP客户端是一个用于连接MCP服务的Java客户端库。它提供了一个简单的API来访问MCP服务，并支持多个服务连接和自动重连。

## 特性

- 自动配置与集成：只需引入依赖并配置MCP服务地址即可使用
- 支持多服务连接：可同时连接多个MCP服务
- 自动心跳检测：定时检测服务连接状态，自动重连断开的服务
- 简单易用的API：提供丰富的API来执行数据库操作
- 异常处理：提供统一的异常处理机制

## 快速开始

### 添加依赖

```xml
<dependency>
    <groupId>org.xue</groupId>
    <artifactId>mcp-client</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 配置MCP服务

在application.yml中添加MCP服务配置：

```yaml
# MCP客户端配置
mcp:
  enabled: true
  heartbeat:
    interval: 10000 # 心跳检查间隔（毫秒）
  servers:
    mysql:  # 服务名称，可自定义
      url: http://localhost:50941  # MCP服务地址
      retry:
        enabled: true  # 是否启用重试
        interval: 10000  # 重试间隔（毫秒）
    redis:  # 另一个服务
      url: http://localhost:50942
      retry:
        enabled: true
        interval: 10000
```

### 使用MCP客户端

```java
@Service
public class MyService {
    
    @Autowired
    private McpClientTemplate mcpTemplate;
    
    public List<Map<String, Object>> getUserList() {
        // 执行SQL查询
        return mcpTemplate.query("mysql", "SELECT * FROM users");
    }
    
    public int createUser(String name, String email) {
        // 执行带参数的SQL更新
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("email", email);
        
        return mcpTemplate.update("mysql", 
                "INSERT INTO users (name, email) VALUES (:name, :email)", 
                params);
    }
    
    public List<String> getTables() {
        // 获取所有表名
        return mcpTemplate.listTables("mysql");
    }
}
```

## 接口说明

MCP客户端提供以下主要接口：

- `query`：执行SQL查询
- `update`：执行SQL更新（INSERT/UPDATE/DELETE）
- `executeBatch`：批量执行SQL语句（在事务中）
- `listTables`：获取所有表名
- `getTableSchema`：获取表结构
- `getDatabaseMetadata`：获取数据库元数据
- `getQueryMetadata`：获取查询元数据
- `executeRpc`：执行自定义RPC方法
- `isServerAvailable`：检查服务是否可用
- `getServersStatus`：获取所有服务状态信息

## 异常处理

MCP客户端提供两种异常类型：

- `McpClientException`：客户端异常，通常表示客户端配置错误或连接问题
- `McpServerException`：服务端异常，通常表示服务端执行请求时发生错误
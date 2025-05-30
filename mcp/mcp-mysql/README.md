# MCP MySQL

MCP MySQL是一个微服务，提供了MySQL数据库的JSON-RPC接口，支持远程执行SQL查询、更新和获取元数据等操作。

## 特性

- JSON-RPC接口：提供标准的JSON-RPC 2.0接口
- API模式描述：自动生成API模式描述，便于集成
- 参数化SQL：支持命名参数的SQL语句
- 事务支持：批量操作时支持事务
- 简单集成：与MCP Client无缝集成

## API接口

MCP MySQL提供两个主要的API接口：

1. `/api/schema/db`：获取API模式描述
2. `/api/rpc/db`：执行JSON-RPC请求

### API模式描述

可以通过以下方式获取API模式描述：

```
GET /api/schema/db?format=rpc_json
```

支持的格式有：
- `rpc_json`：JSON-RPC格式（默认）
- `function_calling`：函数调用格式

### JSON-RPC接口

所有的数据库操作都通过JSON-RPC接口执行，例如：

```
POST /api/rpc/db
Content-Type: application/json

{
  "jsonrpc": "2.0",
  "method": "query",
  "params": {
    "sql": "SELECT * FROM users WHERE age >= :minAge",
    "params": {
      "minAge": 18
    }
  },
  "id": 1
}
```

## 支持的方法

MCP MySQL支持以下RPC方法：

- `query`：执行SQL查询
- `update`：执行SQL更新（INSERT/UPDATE/DELETE）
- `batch`：批量执行SQL语句（在事务中）
- `listTables`：获取所有表名
- `getTableSchema`：获取表结构
- `getDatabaseMetadata`：获取数据库元数据
- `getQueryMetadata`：获取查询元数据
- `heartbeat`：检查服务是否可用

## 配置与部署

在`application.yml`中配置数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database
    username: your_username
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

以及服务端口：

```yaml
server:
  port: 50941
```

## 与MCP Client集成

MCP MySQL可以与MCP Client无缝集成，只需在MCP Client的配置中添加：

```yaml
mcp:
  servers:
    mysql:
      url: http://localhost:50941
      retry:
        enabled: true
        interval: 10000  # 毫秒
```

然后就可以使用MCP Client的API来访问MySQL数据库：

```java
@Autowired
private McpClientTemplate mcpTemplate;

// 执行查询
List<Map<String, Object>> users = mcpTemplate.query("mysql", 
                                                   "SELECT * FROM users WHERE age >= :minAge", 
                                                   Map.of("minAge", 18));

// 执行更新
int affected = mcpTemplate.update("mysql", 
                                 "INSERT INTO users (name, age) VALUES (:name, :age)", 
                                 Map.of("name", "张三", "age", 25));
```
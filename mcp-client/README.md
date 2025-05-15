
# MCP-Client 模块分析

MCP-Client 是一个客户端模块，主要用于通过 JSON-RPC 协议与远程 MCP-MySQL 服务进行通信。该模块提供了一个简洁的 API 接口，使应用程序能够方便地执行 SQL 查询、更新操作以及获取数据库元数据等功能。

## 核心组件

### 1. MCPDatabaseService 接口

这是一个使用 JSON-RPC 协议定义的接口，包含以下主要方法：

- `executeQuery`: 执行 SQL 查询
- `executeUpdate`: 执行 SQL 更新（INSERT/UPDATE/DELETE）
- `executeBatch`: 批量执行 SQL 语句（在事务中）
- `listTables`: 获取所有表名
- `getTableSchema`: 获取表结构
- `getDatabaseMetadata`: 获取数据库元数据
- `getQueryMetadata`: 获取查询元数据
- `heartbeat`: 心跳检查，用于检查服务是否可用

### 2. MCPDatabaseServiceWrapper 类

这是对 MCPDatabaseService 的包装类，提供了更友好的 API：

- 简化了参数传递
- 处理了返回结果的转换
- 提供了更直观的方法名称

### 3. JsonRpcClientConfig 配置类

负责配置和创建 JSON-RPC 客户端：

- 从配置文件中读取 MCP-MySQL 服务的 URL
- 创建 JsonRpcHttpClient 实例
- 设置连接超时和读取超时
- 提供降级服务实现，当远程服务不可用时返回友好的错误信息

### 4. ConnectionMonitor 连接监控组件

定期检查与 MCP-MySQL 服务的连接状态：

- 使用 `@Scheduled` 注解定期执行检查
- 通过多种方式尝试检测服务可用性（JSON-RPC 心跳、REST API 心跳、元数据检查）
- 当检测到服务不可用时，尝试重新连接
- 使用反射技术动态更新服务代理，无需重启应用

## 使用示例

ExampleService 类展示了如何使用 MCPDatabaseServiceWrapper：

```java
// 查询示例
public List<Map<String, Object>> getUsersByAge(int minAge) {
    SqlParamBuilder builder = SqlParamBuilder.create(
            "SELECT * FROM users WHERE age >= :minAge ORDER BY age");
    builder.param("minAge", minAge);
    
    return mcpDb.query(builder.getSql(), builder.getParams());
}

// 更新示例
public boolean createUser(String username, int age) {
    SqlParamBuilder builder = SqlParamBuilder.create(
            "INSERT INTO users (username, age) VALUES (:username, :age)");
    builder.param("username", username)
           .param("age", age);
    
    int affected = mcpDb.update(builder.getSql(), builder.getParams());
    return affected > 0;
}

// 批量操作示例
public int createUsers(List<Map<String, Object>> users) {
    List<SqlParamBuilder> builders = new ArrayList<>();
    
    for (Map<String, Object> user : users) {
        SqlParamBuilder builder = SqlParamBuilder.create(
                "INSERT INTO users (username, age) VALUES (:username, :age)");
        builder.param("username", user.get("username"))
               .param("age", user.get("age"));
        builders.add(builder);
    }
    
    return mcpDb.executeBatch(SqlParamBuilder.createBatchParams(builders));
}
```

## 容错机制

该模块实现了多种容错机制：

1. **服务降级**：当远程服务不可用时，返回友好的错误信息而不是抛出异常
2. **自动重连**：定期检查连接状态，并在服务恢复时自动重新连接
3. **多种检测方式**：通过多种方式（JSON-RPC、REST API）检测服务可用性
4. **超时设置**：设置合理的连接超时和读取超时，避免长时间等待

## 总结

MCP-Client 模块是一个设计良好的客户端库，具有一下特点：

1. 提供了简洁易用的 API 接口
2. 实现了完善的容错和自动恢复机制
3. 使用 JSON-RPC 协议与远程服务通信，支持各种数据库操作
4. 通过包装类和工具类简化了开发流程

这个模块使应用程序能够方便地与远程 MCP-MySQL 服务进行交互，同时处理了各种异常情况，提高了系统的稳定性和可用性。
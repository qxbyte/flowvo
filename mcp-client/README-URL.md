# MCP客户端URL路径处理优化

## 背景

为了提高代码的可维护性和封装性，我们对MCP客户端的URL路径处理逻辑进行了优化。之前的设计中，URL拼接逻辑分散在多个地方，包括Agent模块，这导致了耦合和潜在的不一致问题。

## 修改内容

1. **灵活的服务名称配置**：
   - 在`McpProperties.ServerConfig`类中保留`name`属性，用于API路径拼接
   - 如果配置了`name`属性，则使用配置的名称作为服务名
   - 如果未配置`name`属性，则回退使用配置节点名（Map的key）作为服务名
   - 例如：配置了`mcp.servers.mysql.name=mcp-mysql`，则使用`mcp-mysql`作为服务名

2. **URL构建逻辑集中化**：
   - 在`ConnectionManager`类中，URL构建为：`baseUrl + "/" + serviceName`
   - 在`McpServer`类中添加了获取API URL的方法：
     - `getSchemaUrl()` - 获取Schema API URL
     - `getSchemaUrl(String format)` - 获取带格式参数的Schema API URL
     - `getRpcUrl()` - 获取RPC API URL
     - `getServerName()` - 获取服务名称（优先使用配置的name）

3. **统一URL格式**：
   - 统一使用`{baseUrl}/{serviceName}/api/schema`和`{baseUrl}/{serviceName}/api/rpc`格式
   - 移除之前URL中的`/db`后缀

## 使用示例

使用优化后的API获取URL：

```java
// 获取Schema URL
String schemaUrl = mcpTemplate.getSchemaUrl("mysql", "function_calling");
// 结果: http://localhost:8081/mcp-mysql/api/schema?format=function_calling

// 获取RPC URL
String rpcUrl = mcpTemplate.getRpcUrl("mysql");
// 结果: http://localhost:8081/mcp-mysql/api/rpc
```

配置示例：

```yaml
mcp:
  servers:
    mysql:  # 服务节点名
      name: mcp-mysql  # 服务名称，用于URL拼接
      remote: true
      host: localhost
      port: 50941
      protocol: http
```

## 优势

1. **灵活配置**：可以通过`name`属性自定义服务名，也可以使用默认的节点名
2. **关注点分离**：URL构建逻辑集中在MCP客户端，而不是在使用方
3. **统一接口**：提供统一的方法获取不同类型的API URL
4. **向后兼容**：对于未配置name的服务，会回退使用服务标识符

## 影响

此次修改后，使用MCP客户端的应用程序应该通过`McpClientTemplate`提供的方法获取URL，而不是自己构建URL。这样可以确保URL格式的一致性，并减少代码重复。

## 总结

### 主要变更点

1. **URL构建逻辑**：
   - 从`http://localhost:port/api/schema/db`格式
   - 改为`http://localhost:port/serviceName/api/schema`格式

2. **配置方式**：
   - 添加`name`属性用于自定义服务名
   - 在application.yml中使用`mcp.servers.mysql.name=mcp-mysql`进行配置

3. **代码调整**：
   - 在`ConnectionManager`中集中处理URL拼接
   - 在`McpServer`中简化URL构建方法，专注于API路径部分

### 注意事项

1. **配置兼容性**：
   - 如果已有服务未配置`name`属性，默认使用配置节点名
   - 保留对`url`属性的支持，确保向后兼容

2. **依赖项**：
   - 确保Agent模块直接引用MCP客户端
   - 使用标准化的URL获取方法，而不是自行拼接URL

3. **部署注意**：
   - 更新配置文件，添加适当的`name`属性
   - 确保URL路径与服务端一致 
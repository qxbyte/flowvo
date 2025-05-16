# MCP客户端配置指南

## 配置概述

MCP客户端支持两种服务调用模式：
1. **远程模式**：连接到指定host和port的远程服务
2. **本地模式**：连接到本地应用的server.port服务

## 配置示例

```yaml
# MCP客户端配置
mcp:
  enabled: true
  heartbeat:
    interval: 10000 # 心跳检查间隔（毫秒）
  servers:
    # MySQL服务配置
    mysql:
      # 远程模式配置
      remote: true           # 设置为true时使用远程模式，false时使用本地模式
      host: db.example.com   # 远程主机名或IP（仅远程模式下使用）
      port: 50941            # 远程端口号（仅远程模式下使用）
      protocol: http         # 协议（http或https）
      retry:
        enabled: true
        interval: 10000
```

## 配置属性说明

### 核心配置项

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `remote` | 是否使用远程模式，false表示使用本地模式 | true |
| `host` | 远程服务的主机名或IP地址（仅远程模式下使用） | localhost |
| `port` | 远程服务的端口号（仅远程模式下使用） | 8080 |
| `protocol` | 连接协议，可选http或https | http |
| `url` | 完整的服务URL（优先级高于host+port，兼容旧配置） | 无 |

### 重试配置项

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `retry.enabled` | 是否启用重试 | true |
| `retry.interval` | 重试间隔（毫秒） | 10000 |
| `retry.maxRetries` | 最大重试次数，-1表示无限重试 | -1 |

### 其他配置项

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `connectTimeout` | 连接超时时间（毫秒） | 5000 |
| `readTimeout` | 读取超时时间（毫秒） | 10000 |
| `type` | 服务类型 | generic |

## 本地模式说明

当配置`remote: false`时，客户端将使用以下规则构建服务URL：
1. 使用`localhost`作为主机名
2. 使用当前应用的`server.port`作为端口号
3. 使用配置的`protocol`（默认为http）

这使得在同一应用中引入MCP服务端和客户端时，可以自动连接，无需额外配置。

## 优化细节

1. **URL优先级**：
   - 如果明确设置了`url`属性，则优先使用该值
   - 否则，根据`remote`标志和其他属性构建URL

2. **本地端口获取**：
   - 在应用启动时，从Environment中获取`server.port`属性
   - 如果未找到，则使用默认端口8080

3. **日志增强**：
   - 根据不同的连接模式，提供更明确的日志信息
   - 区分远程连接和本地连接的日志输出

4. **向后兼容**：
   - 保留了对旧版配置格式的支持
   - 通过`url`属性可以直接指定完整的服务URL 
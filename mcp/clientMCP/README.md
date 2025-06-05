# Spring AI MCP Client - 容错优化版本

基于Spring AI 1.0.0的MCP (Model Context Protocol) 客户端应用程序，经过全面容错优化，**确保MCP服务器故障不影响客户端启动和运行**。

## 🚀 主要特性

### 核心功能
- **📡 MCP协议支持**: 完整的MCP 1.0.0协议实现
- **🛡️ 故障隔离**: MCP服务器故障不影响客户端启动
- **🔄 延迟连接**: 应用先启动成功，再异步连接MCP服务器
- **🛠️ 工具发现**: 智能发现和枚举远程工具
- **⚡ 工具调用**: 支持实时工具调用和执行
- **🔗 持久连接**: SSE持久连接管理

### 容错特性 (v2.1)
- **🚫 启动隔离**: 服务器不可用时客户端仍能正常启动
- **🔄 自动重试**: 智能重连机制，最多10次重试
- **💚 健康监控**: 60秒定期健康检查和状态监控
- **⚡ 性能优化**: 响应时间监控和连接质量评估
- **🛡️ 增强错误处理**: 详细的错误分类和恢复建议
- **📈 健康评分**: 实时系统健康评分和评估
- **🔧 手动重连**: 支持手动触发重连操作

## 📦 技术栈

- **Spring Boot**: 3.5.0
- **Spring AI**: 1.0.0 (MCP Client)
- **Java**: 17+
- **传输协议**: SSE (Server-Sent Events)
- **配置格式**: YAML
- **异步支持**: @Async + @Scheduled

## 🛡️ 容错架构设计

### 延迟连接策略
```
应用启动流程：
1. ✅ Spring Boot应用启动（不依赖MCP）
2. ✅ Web服务器启动，API端点可用
3. 🔄 后台异步尝试连接MCP服务器
4. 📊 定期重试和健康检查
```

### 连接状态管理
- **🔴 DISCONNECTED**: 未连接（初始状态）
- **🟡 CONNECTING**: 连接中
- **🟢 CONNECTED**: 已连接
- **❌ FAILED**: 连接失败
- **🔄 RECOVERING**: 恢复中

### 故障处理策略
```yaml
容错配置:
  延迟初始化: true          # 不在启动时阻塞
  最大重试次数: 10          # 自动重试限制
  重试间隔: 30秒           # 重试间隔
  健康检查: 60秒           # 定期健康检查
  连接超时: 5秒            # 快速失败
  请求超时: 10秒           # 缩短等待时间
```

## 🔧 配置说明

### application.yml 关键配置
```yaml
spring:
  ai:
    mcp:
      client:
        enabled: true
        lazy-initialization: true    # 🔑 延迟初始化
        fail-fast: false            # 🔑 不快速失败
        sse:
          connections:
            server1:
              url: http://localhost:9091
              retry:
                max-attempts: 3
                initial-delay: 1s
        options:
          request-timeout: 10s       # 缩短超时
          connect-timeout: 5s        # 快速连接检测
          initialized: false         # 🔑 不强制初始化
          fault-tolerance:
            enabled: true            # 🔑 启用容错
```

### 日志级别优化
```yaml
logging:
  level:
    org.springframework.ai.mcp: WARN  # 减少错误日志噪音
    org.springframework.ai.mcp.client: WARN
```

## 🚀 快速开始

### 1. 启动客户端（无需等待服务器）
```bash
./mvnw spring-boot:run
```

**✅ 关键优势**: 即使fileMCP服务器未启动，客户端也能正常启动并提供API服务！

### 2. 检查启动状态
```bash
# 客户端健康检查（始终返回UP）
curl http://localhost:9092/api/mcp/health

# MCP连接状态检查
curl http://localhost:9092/api/mcp/connection/status

# 综合状态检查
curl http://localhost:9092/api/mcp/status/complete
```

### 3. 启动fileMCP服务器（可选）
```bash
cd ../fileMCP
./mvnw spring-boot:run
```

### 4. 验证连接自动建立
```bash
# 等待几秒后检查连接状态
curl http://localhost:9092/api/mcp/handshake

# 发现工具
curl http://localhost:9092/api/mcp/tools/discover
```

## 📋 API端点

### 核心功能端点
| 端点 | 方法 | 描述 | 容错行为 |
|------|------|------|----------|
| `/api/mcp/test` | GET | 测试端点路由 | ✅ 始终可用 |
| `/api/mcp/handshake` | GET | 协议握手检查 | 🔄 显示连接状态 |
| `/api/mcp/tools/discover` | GET | 工具发现 | 📊 返回可用性信息 |
| `/api/mcp/tools/call` | POST | 工具调用测试 | ⚠️ 检查连接状态 |
| `/api/mcp/connection/status` | GET | 连接状态检查 | 📈 详细状态报告 |
| `/api/mcp/status/complete` | GET | 综合状态检查 | 🎯 完整诊断信息 |
| `/api/mcp/health` | GET | 健康检查 | ✅ 客户端始终健康 |
| `/api/mcp/connection/reconnect` | POST | 手动重连 | 🔄 触发重连尝试 |

### 管理端点
| 端点 | 描述 |
|------|------|
| `/actuator/health` | Spring Boot健康检查 |
| `/actuator/info` | 应用信息 |
| `/actuator/metrics` | 监控指标 |

## 📊 监控和诊断

### 健康评分系统
- **EXCELLENT (75-100%)**: 全功能正常运行
- **GOOD (50-74%)**: 基本功能正常
- **POOR (25-49%)**: 部分功能受限
- **CRITICAL (0-24%)**: 严重故障
- **DISABLED (0%)**: MCP客户端已禁用

### 连接质量评估
- **EXCELLENT**: 响应时间 < 100ms
- **GOOD**: 响应时间 < 500ms  
- **FAIR**: 响应时间 < 1000ms
- **POOR**: 响应时间 >= 1000ms

### 状态监控指标
```json
{
  "connectionStatus": "🟢 已连接",
  "qualityAssessment": "EXCELLENT",
  "responseTime": "5ms",
  "healthScore": "100%",
  "retryCount": 0,
  "lastError": "",
  "availableTools": 11
}
```

## 🔧 故障排除

### 常见场景处理

#### 1. 服务器未启动（最常见）
```
❌ 问题: fileMCP服务器未运行
✅ 客户端行为: 正常启动，显示连接失败状态
🔄 自动处理: 每30秒自动重试连接
📊 用户操作: 查看连接状态，启动服务器后自动连接
```

#### 2. 服务器启动后停止
```
❌ 问题: 运行中的服务器停止
✅ 客户端行为: 健康检查检测到故障
🔄 自动处理: 切换到FAILED状态，开始重试
📊 用户操作: 重启服务器或等待自动重连
```

#### 3. 网络中断
```
❌ 问题: 网络连接中断
✅ 客户端行为: 连接超时，切换到恢复模式
🔄 自动处理: 定期检测网络可达性
📊 用户操作: 网络恢复后自动重连
```

### 手动干预操作
```bash
# 手动触发重连
curl -X POST http://localhost:9092/api/mcp/connection/reconnect

# 检查详细连接信息
curl http://localhost:9092/api/mcp/connection/status

# 获取完整诊断报告
curl http://localhost:9092/api/mcp/status/complete | jq .
```

### 日志诊断
```bash
# 查看实时日志
tail -f logs/mcp-client.log

# 搜索连接相关日志
grep "MCP" logs/mcp-client.log | tail -20

# 查看错误信息
grep "FAILED\|ERROR" logs/mcp-client.log
```

## 📈 性能指标

### 启动性能
- **应用启动**: < 3秒（不等待MCP连接）
- **API可用性**: 立即可用
- **MCP连接**: 后台异步进行

### 运行时性能
- **健康检查**: < 5ms
- **状态查询**: < 10ms
- **重连检测**: < 5秒
- **自动重试**: 30秒间隔

### 容错性能
- **故障检测**: < 5秒
- **重连尝试**: < 10秒
- **服务恢复**: 自动检测

## 🔄 版本历史

### v2.1 - 容错优化版本 (2025-06-05)
- 🛡️ **故障隔离**: MCP服务器故障不影响客户端启动
- 🔄 **延迟连接**: 应用先启动，再异步连接服务器
- 💚 **智能重试**: 自动重连机制和健康监控
- 📊 **状态管理**: 完整的连接状态和质量评估
- 🔧 **手动控制**: 支持手动触发重连操作
- 📈 **监控增强**: 详细的诊断信息和性能指标

### v2.0 - 优化版本 (2025-06-05)
- ✨ 全面的日志系统优化
- 📊 结构化状态监控
- ⚡ 性能监控和评估
- 🛡️ 增强的错误处理
- 📈 健康评分系统

### v1.0 - 基础版本
- 🚀 基本MCP客户端功能
- 🔌 SSE连接支持
- 🛠️ 工具发现和调用

## 💡 使用建议

### 开发环境
```bash
# 先启动客户端（无需等待）
./mvnw spring-boot:run

# 随时启动服务器，自动连接
cd ../fileMCP && ./mvnw spring-boot:run
```

### 生产环境
```bash
# 确保容错配置启用
spring.ai.mcp.client.lazy-initialization=true
spring.ai.mcp.client.fail-fast=false

# 监控连接状态
curl /api/mcp/health
```

### 调试模式
```bash
# 启用详细日志
export LOGGING_LEVEL_ORG_MCP_CLIENTMCP=DEBUG

# 实时监控连接
watch -n 5 'curl -s localhost:9092/api/mcp/connection/status | jq .connectionStatus'
```

## 📞 技术支持

### 故障诊断步骤
1. 检查客户端健康: `/api/mcp/health`
2. 查看连接状态: `/api/mcp/connection/status`
3. 运行完整诊断: `/api/mcp/status/complete`
4. 检查应用日志: `logs/mcp-client.log`
5. 手动重连尝试: `POST /api/mcp/connection/reconnect`

### 常见问题FAQ
**Q: 客户端启动时报错MCP连接失败？**
A: 这是正常行为！v2.1版本设计为先启动客户端，后连接服务器，确保应用可用性。

**Q: 如何知道MCP服务器何时可用？**
A: 查看`/api/mcp/connection/status`端点，状态会从"连接失败"自动变为"已连接"。

**Q: 重试次数达到上限后如何处理？**
A: 使用`POST /api/mcp/connection/reconnect`手动触发重连，或重启客户端重置计数器。

---

**🎯 核心优势**: 此版本解决了MCP服务器故障导致客户端无法启动的问题，提供了真正的容错和高可用架构设计。 
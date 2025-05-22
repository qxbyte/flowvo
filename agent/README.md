# Agent服务

Agent服务是一个智能代理服务，用于控制MCP客户端与大模型（如GPT-4）的交互，实现自动化的多轮对话和服务调用。

## 主要功能

1. **接收用户请求**：通过REST接口接收用户的自然语言问题
2. **获取API描述**：从MCP服务获取可用方法的模式描述（schema）
3. **构建工具列表**：将API模式描述转换为大模型可用的工具列表
4. **调用大模型**：将用户问题和工具列表发送给大模型
5. **处理工具调用**：解析大模型返回的工具调用请求，执行对应的MCP函数
6. **多轮交互**：支持大模型与MCP服务之间的多轮对话，直到获得最终回复

## 技术实现

- **Spring Boot**：提供Web服务和依赖注入框架
- **MCP客户端**：用于与MCP服务进行交互
- **OpenAI API**：调用大模型进行对话和函数调用

## 工作流程

1. 用户发送自然语言问题到Agent服务的REST接口
2. Agent服务通过MCP客户端获取API模式描述
3. Agent服务构建大模型请求，包含系统提示词、用户问题和工具列表
4. 大模型分析请求并决定是否调用工具
5. 如果调用工具，Agent服务将请求转发到对应的MCP服务
6. Agent服务将MCP服务的响应作为工具结果返回给大模型
7. 重复步骤4-6，直到大模型返回最终回复或达到最大交互次数

## 配置说明

在`application.yml`中可以配置以下参数：

```yaml
agent:
  enabled: true                # 是否启用Agent
  maxInteractions: 10          # 最大交互次数
  defaultModel: gpt-4-turbo    # 默认使用的大模型
  systemPrompt: "..."          # 系统提示词
  temperature: 0.7             # 大模型温度参数
  llmApi:
    url: "..."                 # 大模型API URL
    key: "..."                 # 大模型API Key
    timeout: 60000             # 超时时间（毫秒）

mcp:
  enabled: true                # 是否启用MCP客户端
  servers:
    mysql:                     # 服务名称
      remote: false            # 是否远程服务（false表示本地模式）
      protocol: http           # 协议
      # 重试配置
      retry:
        enabled: true
        interval: 10000        # 重试间隔（毫秒）
```

## API接口

### 处理用户请求

```
POST /api/agent/process
```

请求体：

```json
{
  "query": "用户的自然语言问题",
  "service": "MCP服务名称",
  "model": "模型名称（可选）",
  "temperature": 0.7 // 温度参数（可选）
}
```

响应：

```json
{
  "status": "success/error/warning",
  "content": "大模型最终回复",
  "message": "错误信息（仅在出错时）",
  "interactions": 3, // 交互次数
  "totalTokens": 1200 // 总token消耗
}
```

## 部署和运行

1. 确保Java 17或更高版本已安装
2. 配置`application.yml`文件中的相关参数
3. 设置环境变量`OPENAI_API_KEY`为你的OpenAI API密钥
4. 运行应用程序：`./mvnw spring-boot:run`

## 注意事项

- 需要确保MCP服务已正确配置并可访问
- 大模型API调用需要有效的API密钥
- 当使用本地模式(`remote: false`)时，会使用`localhost`和当前应用的端口作为目标服务地址 
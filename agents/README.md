# Flowvo - 基于Spring AI 1.0.0和Milvus的文档管理系统

## 项目概述

Flowvo是一个基于Spring AI 1.0.0和Milvus向量数据库的企业级文档管理系统，支持多种文档格式的解析、向量化存储和智能检索。

## 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend UI   │    │   app模块       │    │   agents模块    │
│                 │────│   (8080)        │────│   (8081)        │
│                 │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │                        │
                              │                        │
                       ┌─────────────┐         ┌─────────────┐
                       │   文档解析   │         │   Milvus    │
                       │   服务      │         │   向量数据库  │
                       └─────────────┘         └─────────────┘
```

## 模块说明

### agents模块 (端口: 8081)
- **功能**: 文档向量化和向量数据库操作
- **技术栈**: Spring AI 1.0.0, Milvus, OpenAI Embedding
- **职责**: 
  - 文档切分和向量化
  - 向量数据存储和检索
  - 语义相似性搜索
  - 用户数据隔离

### app模块 (端口: 8080)
- **功能**: 文档管理前端服务
- **技术栈**: Spring Boot, OpenFeign, Apache POI, PDFBox
- **职责**:
  - 文档上传和解析
  - 多格式文档支持
  - 调用agents模块进行向量化
  - 用户界面API

## 支持的文档格式

### Office文档
- Word: `.doc`, `.docx`
- Excel: `.xls`, `.xlsx` 
- PowerPoint: `.ppt`, `.pptx`

### PDF文档
- PDF文件解析和文本提取

### 文本文档
- `.txt` (纯文本)
- `.csv` (逗号分隔值)
- `.md`, `.markdown` (Markdown格式)
- `.json` (JSON格式)
- `.xml` (XML格式)
- `.yaml`, `.yml` (YAML格式)
- `.log` (日志文件)

## 核心特性

### 1. 智能文档解析
- 支持多种文档格式
- 保持文档结构和语义
- 自动内容提取和清理

### 2. 向量化存储
- 基于Spring AI 1.0.0
- 使用OpenAI embedding模型
- 存储到Milvus向量数据库
- 1536维向量空间

### 3. 语义搜索
- 相似性搜索
- 用户数据隔离
- 可调阈值和结果数量
- 元数据过滤

### 4. 微服务架构
- 模块化设计
- OpenFeign服务调用
- 独立部署和扩展
- 健康检查机制

## 快速开始

### 环境要求
- Java 17+
- Maven 3.6+
- Docker (用于Milvus)
- OpenAI API Key

### 1. 启动Milvus数据库
```bash
# 使用Docker启动Milvus
docker run -d --name milvus-standalone \
  -p 19530:19530 -p 9091:9091 \
  -v /path/to/milvus:/var/lib/milvus \
  milvusdb/milvus:latest
```

### 2. 配置环境变量
```bash
export OPENAI_API_KEY=your-openai-api-key
export OPENAI_BASE_URL=https://api.openai.com
```

### 3. 启动agents模块
```bash
cd agents
mvn spring-boot:run
```

### 4. 启动app模块
```bash
cd app  
mvn spring-boot:run
```

### 5. 验证服务状态
```bash
# 检查agents服务
curl http://localhost:8081/api/health

# 检查app服务  
curl http://localhost:8080/api/health
```

## API使用示例

### 1. 上传文档
```bash
curl -X POST "http://localhost:8080/api/v1/documents/upload" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@document.pdf" \
  -F "userId=user123" \
  -F "tags=重要" \
  -F "description=重要工作文档"
```

### 2. 搜索文档
```bash
curl -X POST "http://localhost:8080/api/v1/documents/search?query=技术文档&userId=user123&limit=5"
```

### 3. 获取用户文档
```bash
curl "http://localhost:8080/api/v1/documents/user/user123"
```

### 4. 获取支持的文件类型
```bash
curl "http://localhost:8080/api/v1/documents/supported-types"
```

## 配置说明

### agents模块配置 (agents/src/main/resources/application.yml)
```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: ${OPENAI_BASE_URL}
      embedding:
        options:
          model: text-embedding-ada-002
    vectorstore:
      milvus:
        client:
          host: "localhost"
          port: 19530
          username: "root"
          password: "milvus"
        databaseName: "default"
        collectionName: "doc_chunk"
        embeddingDimension: 1536
```

### app模块配置 (app/src/main/resources/application.yml)
```yaml
agents:
  service:
    url: http://localhost:8081

feign:
  client:
    config:
      default:
        connectTimeout: 30000
        readTimeout: 60000
```

## 开发注意事项

### 1. Spring AI版本兼容性
- 使用Spring AI 1.0.0正式版
- Document类API发生变化，使用builder模式
- SearchRequest使用新的Builder API

### 2. Milvus配置
- 确保向量维度与embedding模型匹配
- 使用COSINE相似性度量
- IVF_FLAT索引类型适合中小规模数据

### 3. 文档解析限制
- PDF限制前50页解析
- Excel限制10个工作表，每表100行20列
- 文本文件限制50000字符

### 4. 用户数据隔离
- 所有API都需要传递userId参数
- 元数据中包含user_id用于过滤
- 确保用户只能访问自己的数据

## 生产部署建议

### 1. 数据库配置
- 使用MySQL存储文档元数据
- Milvus集群部署提高可用性
- 定期备份向量数据

### 2. 安全配置
- 启用Spring Security
- API访问控制和鉴权
- 文件上传安全检查

### 3. 性能优化
- OpenFeign连接池配置
- 文档批量处理
- 向量搜索结果缓存

### 4. 监控告警
- 健康检查端点
- 日志聚合和分析
- 性能指标监控

## 扩展功能

### 1. 支持更多embedding模型
- Azure OpenAI
- 本地部署模型
- 多语言embedding

### 2. 增强搜索功能
- 混合搜索(向量+关键词)
- 搜索结果排序
- 搜索历史记录

### 3. 知识问答(RAG)
- 集成LLM模型
- 上下文增强生成
- 多轮对话支持

## 贡献指南

1. Fork项目
2. 创建特性分支
3. 提交变更
4. 创建Pull Request

## 许可证

MIT License

## 联系方式

如有问题或建议，请提交Issue或联系维护团队。
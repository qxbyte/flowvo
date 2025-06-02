# App模块 - 文档管理前端服务

## 概述

app模块是文档管理系统的前端服务层，集成了文档解析功能和向量化服务调用，为用户提供完整的文档管理体验。

## 功能特性

- **文档解析**: 支持多种格式文档的解析（PDF、Word、Excel、PowerPoint、文本等）
- **文档上传**: 提供文件上传和处理功能
- **向量化调用**: 通过OpenFeign调用agents模块进行文档向量化
- **搜索功能**: 支持向量相似性搜索
- **文档管理**: 提供完整的文档CRUD操作

## 技术栈

- Spring Boot 3.5.0
- Spring Cloud OpenFeign
- Apache POI (Office文档解析)
- PDFBox (PDF文档解析)
- MySQL (数据存储)

## 支持的文档格式

### Office文档
- **Word**: .doc, .docx
- **Excel**: .xls, .xlsx
- **PowerPoint**: .ppt, .pptx

### PDF文档
- PDF文件解析和文本提取

### 文本文档
- .txt (纯文本)
- .csv (逗号分隔值)
- .md, .markdown (Markdown格式)
- .json (JSON格式)
- .xml (XML格式)
- .yaml, .yml (YAML格式)
- .log (日志文件)

## API接口

### 文档上传
```http
POST /api/v1/documents/upload
Content-Type: multipart/form-data

Form Data:
- file: 文档文件
- userId: 用户ID
- tags: 标签数组 (可选)
- description: 描述 (可选)
```

### 文档搜索
```http
POST /api/v1/documents/search?query={query}&userId={userId}&limit={limit}&threshold={threshold}
```

### 获取用户文档
```http
GET /api/v1/documents/user/{userId}
```

### 获取文档详情
```http
GET /api/v1/documents/{documentId}?userId={userId}
```

### 删除文档
```http
DELETE /api/v1/documents/{documentId}?userId={userId}
```

### 获取支持的文件类型
```http
GET /api/v1/documents/supported-types
```

## 配置说明

### 应用配置
```yaml
spring:
  application:
    name: app
  
  # 文件上传配置
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB
      enabled: true

# 服务端口
server:
  port: 8080

# Agents服务配置
agents:
  service:
    url: http://localhost:8081
```

### OpenFeign配置
```yaml
feign:
  client:
    config:
      default:
        connectTimeout: 30000
        readTimeout: 60000
  compression:
    request:
      enabled: true
    response:
      enabled: true
```

## 启动说明

1. 确保MySQL数据库可用
2. 确保agents模块服务运行在localhost:8081
3. 启动app模块服务

```bash
cd app
mvn spring-boot:run
```

服务将在端口8080启动。

## 使用示例

### 上传文档
```bash
curl -X POST "http://localhost:8080/api/v1/documents/upload" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@document.pdf" \
  -F "userId=user123" \
  -F "tags=重要" \
  -F "tags=工作" \
  -F "description=工作相关的重要文档"
```

### 搜索文档
```bash
curl -X POST "http://localhost:8080/api/v1/documents/search?query=技术文档&userId=user123&limit=5"
```

### 获取用户文档列表
```bash
curl -X GET "http://localhost:8080/api/v1/documents/user/user123"
```

## 文档解析器

### WordDocumentParser
- 支持 .doc 和 .docx 格式
- 提取文本内容和段落结构

### PdfDocumentParser  
- 支持PDF文件解析
- 限制解析前50页避免内容过长

### ExcelDocumentParser
- 支持 .xls 和 .xlsx 格式
- 转换为JSON格式存储表格数据

### PowerPointDocumentParser
- 支持 .ppt 和 .pptx 格式
- 提取幻灯片文本内容

### TextDocumentParser
- 支持多种文本格式
- 处理编码和内容长度限制

## 响应格式

所有API都返回统一的响应格式：

```json
{
  "success": true,
  "message": "操作成功",
  "data": {},
  "count": 1
}
```

错误响应：
```json
{
  "success": false,
  "message": "错误信息"
}
```

## 集成说明

app模块通过OpenFeign调用agents模块：

1. **文档解析**: 在app模块本地完成
2. **向量化处理**: 调用agents模块进行文本切分和向量化
3. **存储**: 向量数据存储在Milvus，元数据可存储在MySQL
4. **搜索**: 通过agents模块进行向量相似性搜索

## 注意事项

1. 文件大小限制为100MB
2. 支持的文档格式有限，建议上传前检查
3. 长文档会被截断以避免内存问题
4. 确保agents服务可用才能完成向量化
5. 用户ID用于数据隔离，确保传递正确的用户标识 
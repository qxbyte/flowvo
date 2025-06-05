# 文件操作MCP服务器

基于Spring AI框架实现的文件和文件夹操作MCP (Model Context Protocol) 服务器，遵循JSON RPC 2.0协议，提供全面的文件系统操作工具。

## 功能特性

### 智能文件读取 ⭐
- ✅ **智能文档解析** - 使用Apache Tika技术，支持多种文档格式的内容提取
  - 纯文本文件：`.txt`, `.md`, `.json`, `.xml`, `.yaml`, `.yml`, `.properties`, `.log`, `.csv`
  - Office文档：`.doc`, `.docx`, `.xls`, `.xlsx`, `.ppt`, `.pptx`
  - PDF文档：`.pdf`
  - 开放文档：`.odt`, `.ods`, `.odp`
  - 富文本：`.rtf`
- ✅ **自动格式检测** - 根据文件扩展名自动选择最佳解析方式
- ✅ **降级处理** - Tika解析失败时自动尝试纯文本读取

### 文件操作
- ✅ **创建文件** - 在指定目录创建新文件，支持初始内容
- ✅ **读取文件** - 智能读取各种格式的文件内容
- ✅ **写入文件** - 写入内容到文件，如果文件不存在则创建
- ✅ **删除文件** - 删除指定文件
- ✅ **移动文件** - 移动/重命名文件

### 基于行的文件编辑
- ✅ **删除指定行** - 删除文件中的特定行或行范围
- ✅ **插入内容到指定行** - 在文件的指定行位置插入新内容

### 文件夹操作
- ✅ **创建目录** - 创建新目录（支持递归创建）
- ✅ **删除目录** - 删除目录及其所有内容
- ✅ **列出目录内容** - 显示目录中的文件和子目录

### 信息查询
- ✅ **获取文件/目录信息** - 查看文件或目录的详细信息（大小、权限、修改时间等）
- ✅ **格式支持检测** - 显示文件是否支持Tika解析

## 技术架构

- **Framework**: Spring Boot 3.5.0 + Spring AI 1.0.0
- **Protocol**: Model Context Protocol (MCP) with JSON RPC 2.0
- **Transport**: STDIO (标准输入/输出) 
- **Java Version**: 17+
- **Document Parser**: Apache Tika（支持200+种文档格式）
- **Security**: 路径安全检查，防止目录遍历攻击
- **Configuration**: YAML格式配置文件

## 快速开始

### 1. 构建项目

```bash
# 克隆项目
git clone <repository-url>
cd mcp/fileMCP

# 使用Maven构建
./mvnw clean package

# 或者使用已安装的Maven
mvn clean package
```

### 2. 运行MCP服务器

```bash
# 直接运行JAR文件
java -jar target/fileMCP-0.0.1-SNAPSHOT.jar

# 或者使用Maven插件运行
./mvnw spring-boot:run
```

### 3. 配置基础目录

默认情况下，服务器在 `${user.home}/mcp-files` 目录下操作。可以通过配置文件或环境变量修改：

```yaml
# application.yml
file:
  operations:
    base-path: /path/to/your/workspace
```

或者在启动时指定：

```bash
java -jar target/fileMCP-0.0.1-SNAPSHOT.jar --file.operations.base-path=/path/to/workspace
```

## MCP客户端接入指南

### Claude Desktop 接入

1. **安装Claude Desktop** (如果还没有安装)

2. **配置MCP服务器**

在Claude Desktop配置文件中添加我们的MCP服务器：

**macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`
**Windows**: `%APPDATA%/Claude/claude_desktop_config.json`

```json
{
  "mcpServers": {
    "file-operations": {
      "command": "java",
      "args": [
        "-jar",
        "/path/to/fileMCP-0.0.1-SNAPSHOT.jar"
      ],
      "env": {
        "FILE_OPERATIONS_BASE_PATH": "/path/to/your/workspace"
      }
    }
  }
}
```

3. **重启Claude Desktop**

重启后，Claude将能够使用文件操作工具。

### 通用MCP客户端接入

对于其他支持MCP的客户端，使用以下配置：

```json
{
  "name": "file-operations-mcp-server",
  "version": "1.0.0",
  "transport": "stdio",
  "command": "java",
  "args": ["-jar", "/path/to/fileMCP-0.0.1-SNAPSHOT.jar"]
}
```

### HTTP SSE模式 (可选)

如果需要HTTP传输模式，可以使用WebFlux版本：

1. **修改依赖**

在`pom.xml`中替换为：

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webflux</artifactId>
</dependency>
```

2. **修改配置**

```yaml
spring:
  main:
    web-application-type: reactive
  ai:
    mcp:
      server:
        sse-message-endpoint: /mcp/message
server:
  port: 8080
```

3. **客户端配置**

```json
{
  "file-operations": {
    "url": "http://localhost:8080",
    "transport": "sse"
  }
}
```

## 可用工具

| 工具名称 | 描述 | 参数 | 智能解析 |
|---------|------|------|---------|
| `create_file` | 创建新文件 | `filePath`: 文件路径, `content`: 可选的初始内容 | - |
| `read_file` | 智能读取文件内容 | `filePath`: 文件路径 | ✅ Tika解析 |
| `write_file` | 写入文件内容 | `filePath`: 文件路径, `content`: 要写入的内容 | - |
| `delete_file` | 删除文件 | `filePath`: 文件路径 | - |
| `move_file` | 移动/重命名文件 | `sourcePath`: 源路径, `destinationPath`: 目标路径 | - |
| `delete_lines` | 删除指定行 | `filePath`: 文件路径, `startLine`: 起始行号(1-based), `lineCount`: 删除行数(可选) | - |
| `insert_lines` | 插入内容到指定行 | `filePath`: 文件路径, `lineNumber`: 行号(1-based), `content`: 插入内容 | - |
| `create_directory` | 创建目录 | `directoryPath`: 目录路径 | - |
| `delete_directory` | 删除目录 | `directoryPath`: 目录路径 | - |
| `list_directory` | 列出目录内容 | `directoryPath`: 目录路径 | - |
| `get_file_info` | 获取文件/目录信息 | `filePath`: 文件或目录路径 | ✅ 格式检测 |

## 使用示例

### 与Claude Desktop的交互示例

```
用户: 请帮我读取这个PDF文件的内容

Claude: 我来帮您读取PDF文件内容。

[使用 read_file 工具]
- filePath: "document.pdf"

正在使用Apache Tika解析PDF文档...
PDF内容已成功提取：

[这里显示PDF的文本内容]
```

```
用户: 请创建一个Word文档的摘要

Claude: 我来读取Word文档并创建摘要。

[使用 read_file 工具读取Word文档]
[使用 create_file 工具创建摘要文件]

已成功读取Word文档内容并创建摘要文件。
```

### 智能文档解析示例

```
用户: 分析这个Excel文件的数据

Claude: [使用 read_file 工具]
- filePath: "data.xlsx"

使用Tika解析Excel文档...
提取的表格数据：
[这里显示Excel的文本内容]

基于数据内容的分析：
...
```

### JSON RPC 2.0 API示例

```json
{
  "jsonrpc": "2.0",
  "method": "tools/call",
  "params": {
    "name": "read_file",
    "arguments": {
      "filePath": "document.pdf"
    }
  },
  "id": 1
}
```

响应：

```json
{
  "jsonrpc": "2.0",
  "result": {
    "content": [
      {
        "type": "text",
        "text": "PDF文档的提取内容..."
      }
    ],
    "isError": false
  },
  "id": 1
}
```

## 配置参数

### YAML格式配置 (application.yml)

```yaml
spring:
  ai:
    mcp:
      server:
        name: file-operations-mcp-server
        version: 1.0.0
        instructions: 详细的服务说明
        type: SYNC
        capabilities:
          tool: true
          resource: false
          prompt: false
          completion: false
        tool-change-notification: true
  main:
    web-application-type: none
    banner-mode: off

logging:
  pattern:
    console: ""

file:
  operations:
    base-path: ${user.home}/mcp-files
    max-file-size: 10MB
    allowed-extensions: .txt,.md,.json,.xml,.yaml,.yml,.properties,.log,.csv,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.rtf,.odt,.ods,.odp
```

### 配置参数说明

| 参数 | 默认值 | 描述 |
|------|--------|------|
| `file.operations.base-path` | `${user.home}/mcp-files` | 文件操作的基础目录 |
| `file.operations.max-file-size` | `10MB` | 最大文件大小限制 |
| `file.operations.allowed-extensions` | 见配置文件 | 允许的文件扩展名（包含Tika支持的格式） |
| `spring.ai.mcp.server.name` | `file-operations-mcp-server` | MCP服务器名称 |
| `spring.ai.mcp.server.version` | `1.0.0` | MCP服务器版本 |

## 智能解析支持的格式

### 完全支持（Tika解析）
- **PDF文档**: `.pdf`
- **Microsoft Office**: `.doc`, `.docx`, `.xls`, `.xlsx`, `.ppt`, `.pptx`
- **OpenDocument**: `.odt`, `.ods`, `.odp`
- **富文本**: `.rtf`

### 纯文本支持
- **文本文件**: `.txt`, `.md`, `.log`
- **配置文件**: `.json`, `.xml`, `.yaml`, `.yml`, `.properties`
- **数据文件**: `.csv`
- **代码文件**: `.html`, `.css`, `.js`, `.java`, `.py`, `.php`, `.sql`

### 解析特性
- **自动检测**: 根据文件扩展名自动选择解析方式
- **内容提取**: 从复杂文档格式中提取纯文本内容
- **降级处理**: Tika解析失败时自动降级到纯文本读取
- **格式信息**: `get_file_info`工具显示文件是否支持智能解析

## @Tools注解说明

所有MCP工具都使用`@Tool`注解进行标记，提供：

- **工具名称**: 明确的工具标识符
- **详细描述**: 工具功能和使用场景的详细说明
- **参数说明**: 完整的参数文档和类型定义
- **智能识别**: 帮助AI模型更好地理解和选择合适的工具

这些注解信息会自动传递给MCP客户端，使AI助手能够：
- 更准确地理解每个工具的功能
- 正确选择合适的工具完成任务
- 提供更好的用户交互体验

## 安全性

### 路径安全
- 所有文件操作都限制在配置的基础目录内
- 自动阻止目录遍历攻击（如`../`）
- 路径规范化处理

### 文件类型限制
默认支持的文件扩展名（包含智能解析格式）：
```
.txt, .md, .json, .xml, .yaml, .yml, .properties, .log, .csv,
.pdf, .doc, .docx, .xls, .xlsx, .ppt, .pptx, .rtf, .odt, .ods, .odp
```

可以通过配置修改：

```yaml
file:
  operations:
    allowed-extensions: .txt,.md,.json,.py,.java,.js,.pdf,.docx
```

### 文件大小限制
默认最大文件大小：10MB

```yaml
file:
  operations:
    max-file-size: 50MB
```

## 故障排除

### 常见问题

1. **权限错误**
   - 确保基础目录存在且有读写权限
   - 在Linux/macOS上检查文件权限设置

2. **路径问题**
   - 使用相对路径而不是绝对路径
   - 避免使用`../`等目录遍历字符

3. **编码问题**
   - 默认使用UTF-8编码
   - 确保文件内容为有效的UTF-8格式

4. **文档解析问题**
   - 检查文档是否损坏
   - 确认文件格式是否受支持
   - 查看日志了解Tika解析详情

5. **Claude Desktop连接问题**
   - 检查配置文件路径是否正确
   - 确保JAR文件路径存在
   - 重启Claude Desktop

### 日志调试

启用详细日志：

```yaml
logging:
  level:
    io.mcp.filemcp: DEBUG
    org.springframework.ai: DEBUG
    org.apache.tika: INFO
```

### 验证MCP服务器

使用测试工具验证服务器是否正常工作：

```bash
# 启动服务器后，使用curl测试（需要HTTP模式）
curl -X POST http://localhost:8080 \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","method":"tools/list","params":{},"id":1}'
```

### 测试智能解析

```bash
# 测试PDF文档解析
java -jar target/fileMCP-0.0.1-SNAPSHOT.jar
# 然后使用read_file工具读取PDF文件
```

## 贡献

欢迎提交Issue和Pull Request来改进这个MCP服务器。

### 开发环境

1. Java 17+
2. Maven 3.8+
3. Spring Boot 3.5.0
4. Spring AI 1.0.0

### 运行测试

```bash
./mvnw test
```

新增了18个测试用例，覆盖智能文档解析功能。

## 许可证

[License Information]

## 更新日志

### v1.1.0 (2025-06-04)
- ⭐ 新增智能文档解析功能，支持PDF、Word、Excel等格式
- 🔧 配置文件改为YAML格式，更易读和维护
- 📝 添加@Tool注解，提供详细的工具说明
- 🛡️ 扩展支持的文件格式，增强安全配置
- 📊 优化文件信息显示，支持格式检测
- 🧪 新增18个测试用例，覆盖新功能

### v1.0.0 (2025-06-04)
- 初始版本发布
- 实现所有基础文件操作功能
- 支持STDIO传输模式
- 添加路径安全检查
- 完整的测试覆盖

---

**注意**: 请确保在生产环境中适当配置安全设置，包括基础目录权限和文件类型限制。智能文档解析功能会增加内存使用，请根据需要调整JVM参数。 
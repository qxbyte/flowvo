# FlowVO
## 项目概述
FlowVO 是一个基于微服务架构的智能对话和向量检索平台，集成了自然语言处理、向量数据库和数据管理功能。该平台通过 Function Call 机制处理用户请求，提供智能化服务体验。
## 技术栈
### 前端
- **React**：flowvo-ui-new目录下的新版界面
- **Vue.js 3**：`npm create vue@latest .`
- **UI组件**：@heroicons/vue, @headlessui/vue, @chakra-ui/react
- **工具库**：axios, marked, highlight.js

### 后端
- **Java** + **Spring Boot** + **Spring Cloud**
- **Python**：用于向量嵌入和数据处理
- **Milvus**：向量数据库
- **MySQL**：关系型数据存储（MCP-MySQL服务）
- **Redis**：缓存服务（MCP-Cache）

## 系统架构
    flowvo/
    ├── api/                # API网关
    ├── core/               # 核心服务（业务逻辑、聊天功能）
    ├── app/                # 新核心服务
    ├── agents/             # spring ai组件化智能体
    ├── eureka/             # 服务注册与发现
    ├── mcp/
        ├── fileMCP/        # 文件操作
        ├── mcp-mysql/      # MySQL数据服务
    ├── milvus/             # 向量数据库服务
    ├── python/             # Python服务（嵌入和NLP）
    ├── Floreo/             # 前端应用（Vue.js版本）
    └── flowvo-ui/          # 新版前端应用（React版本）

## 功能模块
### 1. 向量嵌入服务
基于LangChain和Sentence Transformers实现的文本嵌入服务：

_安装必要的python库_

```shell
pip install langchain sentence-transformers fastapi uvicorn
```

_启动服务_
```shell
source embedding_env/bin/activate
cd embedding_service
uvicorn embed_tools_server:app --host 0.0.0.0 --port 8000
```

_API文档_

[http://localhost:8000/docs]()


### 2. Milvus向量数据库
用于高效存储和检索向量数据：

_安装Milvus_
```shell
wget https://github.com/milvus-io/milvus/releases/download/v2.5.10/milvus-standalone-docker-compose.yml -O docker-compose.yml
sudo docker compose up -d
```

_可视化工具（Attu）_
```shell
docker run -d --name attu -p 3000:3000 zilliz/attu
```

### 3. 订单管理模块
提供完整的订单生命周期管理功能：

- **订单创建**：支持创建新订单，自动生成订单号
- **订单查询**：多条件筛选，支持关键字搜索、状态筛选、日期范围筛选
- **订单处理**：修改订单状态、金额等信息
- **订单取消**：支持取消待付款状态的订单
- **数据分页**：高效处理大量订单数据

_技术特点_
- 基于RESTful API设计
- 使用JPA实现数据访问
- 支持复合条件查询
- 实现UUID主键和自动时间戳

### 4. 聊天对话模块
实现智能对话功能：

- **对话管理**：创建、查询、删除、重命名对话
- **消息处理**：发送消息并获取AI回复，支持Markdown格式渲染
- **历史记录**：保存完整对话历史，通过下拉框方便查看和切换
- **多服务支持**：集成不同的对话服务（RPC、API、搜索等）
- **长时间处理**：支持大模型长时间思考，超时时间延长至60秒

#### PixelChatPage功能
专门为PixelChat页面设计的对话功能，具有以下特点：

- **用户认证**：所有操作都需要用户登录，确保数据安全
- **权限控制**：用户只能访问和操作自己的对话
- **直接AI调用**：使用OpenAI客户端直接调用大模型，不依赖MCP工具
- **对话隔离**：PixelChat对话与其他对话分离存储（source="chat"）
- **完整CRUD**：支持创建、读取、更新、删除对话的完整操作

_API端点_
- `POST /api/pixel_chat/conversations` - 创建对话
- `GET /api/pixel_chat/conversations` - 获取用户对话列表
- `GET /api/pixel_chat/conversations/{id}` - 获取对话详情
- `PUT /api/pixel_chat/conversations/{id}/title` - 重命名对话
- `DELETE /api/pixel_chat/conversations/{id}` - 删除对话
- `GET /api/pixel_chat/conversations/{id}/messages` - 获取对话消息
- `POST /api/pixel_chat/send` - 发送消息并获取AI回复

_技术特点_
- 集成Agent服务进行AI交互
- 实现实时消息发送和接收
- 支持多种模型和服务类型
- 响应式设计，适配不同屏幕尺寸
- Markdown渲染支持代码高亮、链接、列表等格式

### 5. MCP服务
实现数据库HTTP+JSON-RPC调用功能，包括：
- MCP-MySQL：数据库访问服务
- fileMCP：文件服务
- MCP-Client：客户端通信工具

## 对话处理流程
### ~~工作流程~~
1. **用户发送消息**：前端发送用户问题（如："请帮我查一下今天上海的天气"）
2. **第一轮请求**：
    - 服务端封装消息和可用的functions
    - 提示模型判断是否需要调用function

3. **决策分支**：
    - 若不需要function call：直接向模型发送问题并返回答案
    - 若需要function call：进入第二轮请求

4. **第二轮请求**（如需）：
    - 发送完整functions JSON和原始消息
    - 模型返回function_call JSON

5. **执行函数**：
    - 后端解析并通过反射调用对应方法
    - 获取函数执行结果

6. **返回结果**：
    - 将函数执行结果作为答案流式返回前端

### mcp调用多轮对话流程示意图

                                    用户输入问题
                                  ｜---- ↓
                                  ｜ 服务端封装问题 + 发送完整 functions JSON + 原始消息给模型                    
                                  ｜     ↓                                   
            根据模型返回解析循环调用  ｜模型返回 function_call JSON          
                                  ｜     ↓  
                                  ｜---- ↓ 
                                    后端解析并调用mcp server           
                                        ↓
                                    函数执行结果作为回答流式返回前端

## 安装部署
### 前端环境
_安装依赖_
```shell
npm install
```
```shell
npm install axios marked highlight.js
```
```shell
npm install @heroicons/vue @headlessui/vue
```

_设置国内镜像（可选）_
```shell
npm config set registry https://registry.npmmirror.com
```

### 后端环境
1. **启动Eureka服务**
2. **启动Core服务**
3. **启动MCP-MySQL服务**
4. **启动MCP-Cache服务**
5. **启动Python嵌入服务**
6. **启动Milvus向量数据库**

## 开发指南
1. 遵循微服务架构设计原则
2. 使用Function Call模式处理复杂用户请求
3. 利用向量数据库实现语义检索和相似度匹配
4. 在Core模块中实现核心业务逻辑
5. 通过MCP服务实现数据访问和缓存操作

## 贡献指南
1. Fork项目仓库
2. 创建功能分支
3. 提交更改
4. 创建Pull Request

## 更新日志

### 2024年12月最新更新

#### 像素风格UI优化 【界面优化】
- **马里奥机器人**：将原有的机器人替换为马里奥风格的像素画角色
  - 红色帽子配M标志
  - 棕色头发和胡子
  - 肤色脸部，黑色眼睛
  - 红色上衣配黄色纽扣
  - 蓝色工装裤
  - 棕色鞋子
  - 尺寸放大至64x72像素，增强视觉效果
  - 保持原有的走路动画和弹跳效果

- **装饰性彩色小方块**：在对话发送栏区域添加动态装饰元素
  - 12种预定义的像素风格颜色
  - 随机大小（6-14px）和位置分布
  - 闪烁动画效果，增加视觉趣味性
  - 每10秒自动重新生成位置，保持动态感
  - 不影响用户交互，纯装饰性设计

#### 后台文档解析功能 【新增功能】
- **文档解析器架构**：
  - 创建了可扩展的文档解析器接口`DocumentParser`
  - 实现了多种文档解析器：PDF、Word、Excel、PowerPoint、文本文档
  - 通过`DocumentParserService`统一管理所有解析器

- **支持的文档格式**：
  - **PDF文档**：使用Apache PDFBox解析，提取文本内容
  - **Word文档**：支持.doc和.docx格式，使用Apache POI解析
  - **Excel表格**：支持.xls和.xlsx格式，转换为结构化JSON数据
  - **PowerPoint演示**：支持.ppt和.pptx格式，按幻灯片提取文本
  - **文本文档**：支持TXT、CSV、JSON、XML、Markdown等格式

- **智能内容处理**：
  - **Excel特殊处理**：将表格数据转换为JSON格式，便于AI分析数据关系
  - **大文档优化**：限制解析内容长度，避免内存溢出
  - **多工作表支持**：Excel文档支持多个工作表解析
  - **幻灯片分组**：PowerPoint文档按幻灯片组织内容结构

- **技术架构**：
  - 添加Apache POI依赖处理Office文档
  - 添加PDFBox依赖处理PDF文件
  - 实现base64内容解析，支持前端上传的文件
  - 集成到PixelChat服务，文档内容直接传递给AI模型

- **前后端协作**：
  - 前端上传Office文档时同时发送base64内容
  - 后端智能识别文档类型并选择合适的解析器
  - UI中不显示解析后的文档内容，保持界面简洁
  - 解析后的内容完整传递给AI进行分析

#### 文件处理和UI优化
- **简化文档附件显示**：
  - 移除消息气泡中的文档内容预览，只显示文件名和大小
  - 保持图片文件的预览功能
  - 界面更整洁，用户体验更好
  - **彻底移除用户发送消息中的文档内容显示**

- **解决大文件内存问题**：
  - 优化文件处理逻辑，对于大文件（>1MB）或Office文件不在前端读取内容
  - 提高文件大小限制从10MB到50MB
  - 添加Spring Boot文件上传配置，支持更大文件
  - 智能处理不同类型文件，避免内存溢出

- **优化Markdown显示样式**：
  - 调整行间距从1.4到1.2，内容更紧凑
  - 减少段落、列表、标题的间距
  - 优化代码块显示，提升阅读体验
  - **大幅度减少各元素间距**：段落间距1px，列表项间距0px，标题间距3px

- **侧边栏日期分组 【新功能】**：
  - 对话记录按日期智能分组：今天、昨天、7天内、更早
  - 分组标题采用像素风格设计，与整体UI保持一致
  - 提升历史对话的导航和查找体验

- **文件上传全面优化 【新功能】**：
  - **拖拽交互优化**：拖拽提示只在输入框获得焦点时显示
  - **扩展文件类型支持**：支持几乎所有常见代码文件（java, vue, py, js, ts, jsx, tsx, cpp, cs, php, rb, go, rs, swift, kt, scala, sh, sql, yml, yaml等）
  - **智能乱码检测**：自动检测文本乱码，避免无效内容存储
  - **Office文档优化**：智能识别Office文档，避免发送二进制给AI，提供合理处理建议
  - **编码优化**：使用UTF-8编码读取文本，减少乱码概率

- **提示词配置化**：
  - 将所有写死在代码中的AI提示词提取到配置文件中

### 2025年5月最新更新
- **UI改进**：
  - 改进了聊天界面设计，使用下拉框替代侧边栏显示对话记录
  - 添加了对话重命名功能，可直接在列表中编辑对话标题
  - 优化对话列表显示，添加删除和重命名按钮
  - 添加Markdown渲染支持，美化AI回复显示效果

- **后端优化**：
  - 修复了大模型回复显示问题，确保错误和警告消息正确显示
  - 将API请求超时时间从10秒增加到60秒
  - 添加"正在思考中，请耐心等待"的提示信息
  - 改进了错误处理机制，确保用户能看到完整的错误信息

- **用户体验改进**：
  - 简化了创建新对话的流程
  - 改进了对话切换的用户体验
  - 添加了更清晰的UI状态提示（加载中、无对话等）

_FlowVO - 智能对话与向量检索平台&&业务MCP_

# 图像识别功能使用指南

## 🖼️ 功能概述

本项目实现了基于 OpenAI Vision API 的图像识别功能，用户可以上传图片并获得AI的智能分析和描述。

## 🏗️ 架构设计

### 后端架构 (app/src/main/java/org/xue/app/chat/)

```
chat/
├── dto/                          # 数据传输对象
│   ├── VisionRequestDTO.java    # 图像识别请求DTO
│   └── VisionResponseDTO.java   # 图像识别响应DTO
├── service/                      # 服务层
│   ├── VisionService.java       # 图像识别服务接口
│   ├── ImageProcessingService.java # 图像处理服务
│   └── impl/
│       └── VisionServiceImpl.java # 图像识别服务实现
├── client/                       # 外部API客户端
│   └── OpenAIVisionClient.java  # OpenAI Vision API客户端
└── controller/                   # 控制器层
    ├── VisionController.java    # 独立图像识别控制器
    └── PixelChatController.java # 聊天集成控制器
```

### 前端架构

- **PixelChatPage.tsx**: 主聊天页面，集成图像识别功能
- **api.ts**: API接口定义和调用函数

## 🚀 快速开始

### 1. 配置 OpenAI API Key

在 `app/src/main/resources/application.yml` 中配置：

```yaml
# OpenAI Vision API 配置
openai:
  api:
    key: your-openai-api-key-here
    url: https://api.openai.com

# 文件上传和图像识别配置
app:
  upload:
    path: uploads
  vision:
    max-file-size: 10485760  # 10MB
    supported-types: image/jpeg,image/jpg,image/png,image/gif,image/webp,image/bmp
    default-model: gpt-4o-mini
    connect-timeout: 30
    read-timeout: 60
    write-timeout: 60
```

### 2. 启动应用

```bash
# 启动后端
cd app
../gradlew bootRun

# 启动前端
cd flowvo-ui-new
npm run dev
```

### 3. 使用图像识别

1. 打开聊天页面
2. 点击 📎 按钮或拖拽图片到页面
3. 选择图片文件（支持 JPEG、PNG、GIF、WebP、BMP）
4. 输入描述文字（可选）
5. 点击发送，AI将自动分析图片并回复

## 📡 API 接口

### 独立图像识别接口

```http
POST /api/vision/recognize
Content-Type: multipart/form-data

Parameters:
- image: MultipartFile (必需)
- message: String (可选，默认："请描述这张图片的内容。")
- model: String (可选，默认："gpt-4o-mini")
- conversationId: String (可选)
```

### 聊天集成接口

```http
POST /api/pixel_chat/vision/recognize
Content-Type: multipart/form-data

Parameters:
- image: MultipartFile (必需)
- message: String (可选)
- model: String (可选)
- conversationId: String (可选)
```

### 检查文件支持

```http
POST /api/vision/check
Content-Type: multipart/form-data

Parameters:
- image: MultipartFile (必需)
```

### 获取支持格式

```http
GET /api/vision/formats
```

## 🔧 技术实现

### 核心组件

1. **ImageProcessingService**: 
   - 图像文件验证
   - 元数据提取
   - Base64编码转换

2. **OpenAIVisionClient**: 
   - OpenAI Vision API调用
   - 请求构建和响应解析
   - 错误处理

3. **VisionService**: 
   - 业务逻辑整合
   - 服务编排

4. **前端集成**: 
   - 自动检测图片文件
   - Base64转File对象
   - API调用和结果展示

### 数据流

```
用户上传图片 → 前端读取为Base64 → 转换为File对象 → 
调用Vision API → OpenAI处理 → 返回识别结果 → 
显示在聊天界面
```

## 🎯 功能特性

- ✅ 支持多种图片格式 (JPEG, PNG, GIF, WebP, BMP)
- ✅ 文件大小限制 (10MB)
- ✅ 自动图片识别
- ✅ 聊天界面集成
- ✅ 错误处理和用户提示
- ✅ 打字机效果显示
- ✅ 代理支持
- ✅ 超时配置

## 🛠️ 配置选项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `openai.api.key` | - | OpenAI API密钥（必需） |
| `openai.api.url` | https://api.openai.com | OpenAI API地址 |
| `app.vision.max-file-size` | 10485760 | 最大文件大小（字节） |
| `app.vision.default-model` | gpt-4o-mini | 默认使用的模型 |
| `app.vision.connect-timeout` | 30 | 连接超时（秒） |
| `app.vision.read-timeout` | 60 | 读取超时（秒） |

## 🐛 故障排除

### 常见问题

1. **API Key未配置**
   - 错误：`OpenAI API Key 未配置`
   - 解决：在配置文件中设置正确的API Key

2. **文件格式不支持**
   - 错误：`图像文件格式不支持或文件过大`
   - 解决：使用支持的图片格式，确保文件小于10MB

3. **网络连接问题**
   - 错误：`OpenAI API 调用失败`
   - 解决：检查网络连接，配置代理（如需要）

4. **服务不可用**
   - 错误：`图像识别服务暂不可用`
   - 解决：确保所有依赖服务正常启动

## 📝 开发说明

### 扩展新功能

1. **添加新的图片格式支持**：
   - 修改 `ImageProcessingService.SUPPORTED_IMAGE_TYPES`

2. **集成其他Vision API**：
   - 实现新的客户端类（如 `GoogleVisionClient`）
   - 在 `VisionServiceImpl` 中添加选择逻辑

3. **添加图片预处理**：
   - 在 `ImageProcessingService` 中添加压缩、裁剪等功能

### 代码规范

- 所有服务类使用接口定义
- 异常处理要完整
- 日志记录要详细
- 配置项要可配置
- 单元测试要覆盖

## 📄 许可证

本项目遵循 MIT 许可证。 
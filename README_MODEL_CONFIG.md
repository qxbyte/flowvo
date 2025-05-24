# AI模型配置指南

## 概述

本项目支持动态配置AI模型，通过修改`application.yml`配置文件即可添加或删除可用的模型。前端会自动从后端API获取模型列表，无需硬编码。

## 配置方式

### 1. OpenAI模型配置

在`application.yml`中配置OpenAI模型：

```yaml
ai:
  openai:
    api-key: ${OPENAI_API_KEY:your-openai-api-key}
    base-url: ${OPENAI_BASE_URL:https://api.openai.com}
    chat:
      options:
        # 多个模型用逗号分隔
        model: ${OPENAI_MODELS:gpt-4o-mini,gpt-4o,gpt-4-turbo,gpt-3.5-turbo}
    proxy:
      enabled: ${OPENAI_PROXY_ENABLED:false}
      host: ${OPENAI_PROXY_HOST:127.0.0.1}
      port: ${OPENAI_PROXY_PORT:7890}
```

### 2. DeepSeek模型配置

```yaml
ai:
  deepseek:
    api-key: ${DEEPSEEK_API_KEY:your-deepseek-api-key}
    base-url: ${DEEPSEEK_BASE_URL:https://api.deepseek.com}
    chat:
      options:
        # 多个模型用逗号分隔
        model: ${DEEPSEEK_MODELS:deepseek-chat,deepseek-coder}
```

### 3. 环境变量配置

推荐使用环境变量来配置敏感信息：

```bash
# OpenAI配置
export OPENAI_API_KEY="sk-your-openai-api-key"
export OPENAI_MODELS="gpt-4o-mini,gpt-4o,gpt-4-turbo,gpt-3.5-turbo"

# DeepSeek配置
export DEEPSEEK_API_KEY="sk-your-deepseek-api-key"
export DEEPSEEK_MODELS="deepseek-chat"

# 代理配置（可选）
export OPENAI_PROXY_ENABLED="true"
export OPENAI_PROXY_HOST="127.0.0.1"
export OPENAI_PROXY_PORT="7890"
```

## 支持的模型

### OpenAI模型
- `gpt-4o` - 最新的多模态模型（支持Vision）
- `gpt-4o-mini` - 快速高效的Vision模型（支持Vision）
- `gpt-4-turbo` - 最强的文本模型（支持Vision）
- `gpt-3.5-turbo` - 平衡性能的模型

### DeepSeek模型
- `deepseek-chat` - DeepSeek聊天模型
- `deepseek-coder` - DeepSeek代码模型

## Vision功能支持

以下模型支持图像识别功能：
- `gpt-4o`
- `gpt-4o-mini`
- `gpt-4-turbo`
- `gpt-4-vision-preview`

**注意：** DeepSeek模型目前不支持Vision功能。

## API端点

### 获取可用模型列表
```
GET /api/pixel_chat/models
```

### 获取支持Vision的模型列表
```
GET /api/pixel_chat/models/vision
```

## 前端使用

前端会自动调用API获取模型列表，无需修改前端代码：

```typescript
// 自动获取模型列表
const response = await pixelChatApi.getAvailableModels();
const models = response.data; // AIModel[]

// 模型数据结构
interface AIModel {
  id: string;           // 模型ID
  name: string;         // 显示名称
  description: string;  // 描述
  provider: string;     // 提供商 (openai/deepseek)
  visionSupported: boolean; // 是否支持Vision
}
```

## 添加新模型

1. 在`application.yml`中的对应provider下添加新模型ID
2. 在`ModelConfigService.java`中添加模型的显示名称和描述
3. 如果支持Vision，在`isVisionSupported`方法中添加模型ID
4. 重启应用，前端会自动获取新的模型列表

## 故障排除

### 模型不显示
1. 检查配置文件中的模型ID是否正确
2. 检查API密钥是否有效
3. 查看后端日志是否有错误信息

### Vision功能不可用
1. 确认选择的模型支持Vision功能
2. 检查OpenAI API密钥权限
3. 确认图片格式和大小符合要求（支持JPEG、PNG、GIF、WebP、BMP，最大10MB）

### 代理配置
如果需要使用代理访问OpenAI API：

```yaml
ai:
  openai:
    proxy:
      enabled: true
      host: 127.0.0.1
      port: 7890
```

## 配置示例

完整的配置示例：

```yaml
ai:
  openai:
    api-key: sk-your-openai-key
    base-url: https://api.openai.com
    chat:
      options:
        model: gpt-4o-mini,gpt-4o,gpt-4-turbo,gpt-3.5-turbo
    proxy:
      enabled: false
      host: 127.0.0.1
      port: 7890
  
  deepseek:
    api-key: sk-your-deepseek-key
    base-url: https://api.deepseek.com
    chat:
      options:
        model: deepseek-chat
```

这样配置后，前端的模型选择器会显示所有配置的模型，并按提供商分组显示。 
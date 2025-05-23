# PixelChat API 文档

## 概述

PixelChat API 提供了完整的对话管理功能，包括创建对话、发送消息、获取回复等。所有API都需要用户认证，确保数据安全。

## 认证

所有API请求都需要在请求头中包含JWT令牌：

```
Authorization: Bearer <your-jwt-token>
```

## API端点

### 1. 创建对话

**POST** `/api/pixel_chat/conversations`

创建一个新的对话。

**请求体：**
```json
{
  "title": "我的新对话",
  "service": "openai",
  "model": "gpt-3.5-turbo"
}
```

**响应：**
```json
{
  "id": "conv-123",
  "title": "我的新对话",
  "userId": "user-456",
  "source": "chat",
  "service": "openai",
  "model": "gpt-3.5-turbo",
  "createdAt": "2023-12-01T10:00:00",
  "updatedAt": "2023-12-01T10:00:00"
}
```

### 2. 获取对话列表

**GET** `/api/pixel_chat/conversations`

获取当前用户的所有对话列表。

**响应：**
```json
[
  {
    "id": "conv-123",
    "title": "我的新对话",
    "userId": "user-456",
    "source": "chat",
    "service": "openai",
    "model": "gpt-3.5-turbo",
    "createdAt": "2023-12-01T10:00:00",
    "updatedAt": "2023-12-01T10:00:00"
  }
]
```

### 3. 获取对话详情

**GET** `/api/pixel_chat/conversations/{id}`

获取指定对话的详细信息。

**路径参数：**
- `id`: 对话ID

**响应：**
```json
{
  "id": "conv-123",
  "title": "我的新对话",
  "userId": "user-456",
  "source": "chat",
  "service": "openai",
  "model": "gpt-3.5-turbo",
  "createdAt": "2023-12-01T10:00:00",
  "updatedAt": "2023-12-01T10:00:00"
}
```

### 4. 重命名对话

**PUT** `/api/pixel_chat/conversations/{id}/title`

更新对话的标题。

**路径参数：**
- `id`: 对话ID

**请求体：**
```json
{
  "title": "新的对话标题"
}
```

**响应：**
```json
{
  "id": "conv-123",
  "title": "新的对话标题",
  "userId": "user-456",
  "source": "chat",
  "service": "openai",
  "model": "gpt-3.5-turbo",
  "createdAt": "2023-12-01T10:00:00",
  "updatedAt": "2023-12-01T10:05:00"
}
```

### 5. 删除对话

**DELETE** `/api/pixel_chat/conversations/{id}`

删除指定的对话及其所有消息。

**路径参数：**
- `id`: 对话ID

**响应：**
- 状态码：204 No Content

### 6. 获取对话消息

**GET** `/api/pixel_chat/conversations/{id}/messages`

获取指定对话的所有消息。

**路径参数：**
- `id`: 对话ID

**响应：**
```json
[
  {
    "id": "msg-123",
    "conversationId": "conv-123",
    "role": "user",
    "content": "你好",
    "createdAt": "2023-12-01T10:01:00"
  },
  {
    "id": "msg-124",
    "conversationId": "conv-123",
    "role": "assistant",
    "content": "你好！我是AI助手，有什么可以帮助你的吗？",
    "createdAt": "2023-12-01T10:01:05"
  }
]
```

### 7. 发送消息

**POST** `/api/pixel_chat/send`

向指定对话发送消息并获取AI回复。

**请求体：**
```json
{
  "conversationId": "conv-123",
  "message": "请介绍一下人工智能"
}
```

**响应：**
```json
{
  "status": "success",
  "assistantReply": "人工智能（Artificial Intelligence，简称AI）是计算机科学的一个分支...",
  "content": "人工智能（Artificial Intelligence，简称AI）是计算机科学的一个分支...",
  "message": "人工智能（Artificial Intelligence，简称AI）是计算机科学的一个分支...",
  "interactions": 1,
  "totalTokens": 0
}
```

## 错误响应

当请求失败时，API会返回相应的错误信息：

**401 未授权：**
```json
{
  "error": "用户未登录"
}
```

**404 未找到：**
```json
{
  "error": "PixelChat Conversation not found with id: conv-123"
}
```

**500 服务器错误：**
```json
{
  "error": "Failed to create conversation: 详细错误信息"
}
```

## 使用示例

### JavaScript/TypeScript

```javascript
// 设置认证头
const headers = {
  'Authorization': `Bearer ${token}`,
  'Content-Type': 'application/json'
};

// 创建对话
const createConversation = async () => {
  const response = await fetch('/api/pixel_chat/conversations', {
    method: 'POST',
    headers,
    body: JSON.stringify({
      title: '新对话',
      service: 'openai',
      model: 'gpt-3.5-turbo'
    })
  });
  return response.json();
};

// 发送消息
const sendMessage = async (conversationId, message) => {
  const response = await fetch('/api/pixel_chat/send', {
    method: 'POST',
    headers,
    body: JSON.stringify({
      conversationId,
      message
    })
  });
  return response.json();
};
```

### cURL

```bash
# 创建对话
curl -X POST http://localhost:8080/api/pixel_chat/conversations \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{"title": "新对话", "service": "openai", "model": "gpt-3.5-turbo"}'

# 发送消息
curl -X POST http://localhost:8080/api/pixel_chat/send \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{"conversationId": "conv-123", "message": "你好"}'
```

## 注意事项

1. **用户权限**：用户只能访问和操作自己创建的对话
2. **数据隔离**：PixelChat对话与其他对话系统分离存储
3. **AI模型**：默认使用gpt-3.5-turbo模型，可以在创建对话时指定其他模型
4. **消息历史**：发送消息时会自动包含对话的历史上下文
5. **错误处理**：所有API都有完善的错误处理机制，确保用户能获得清晰的错误信息 
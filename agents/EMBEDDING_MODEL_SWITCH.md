# 嵌入模型动态切换解决方案 🚀

## 🎯 你的需求
**希望yml配置哪个嵌入模型，VectorStore就注入哪个嵌入模型**

## ✅ 解决方案

我已经为你创建了一个智能的`VectorStoreConfig`，它会根据`embedding.type`配置自动选择对应的嵌入模型：

### 📝 配置文件切换

#### 1️⃣ 使用OpenAI嵌入模型 (如截图配置)
```yaml
# application.yml
embedding:
  type: OPENAI  # 👈 设置为OPENAI

spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      embedding:
        options:
          model: text-embedding-ada-002
    # 确保其他服务被注释掉
#    zhipuai:
#      api-key: ${ZHIPU_API_KEY}
```

#### 2️⃣ 使用ZhiPuAI嵌入模型
```yaml
# application.yml  
embedding:
  type: ZHIPUAI  # 👈 设置为ZHIPUAI

spring:
  ai:
    zhipuai:
      api-key: ${ZHIPU_API_KEY}
      embedding:
        options:
          model: embedding-3
    # 确保其他服务被注释掉  
#    openai:
#      api-key: ${OPENAI_API_KEY}
```

#### 3️⃣ 使用外部嵌入模型  
```yaml
# application.yml
embedding:
  type: EXTERNAL  # 👈 设置为EXTERNAL
  external:
    url: http://localhost:8000

# 注释掉所有Spring AI服务
spring:
  ai:
#    openai:
#      api-key: ${OPENAI_API_KEY}
#    zhipuai:
#      api-key: ${ZHIPU_API_KEY}
```

## 🔧 工作原理

`VectorStoreConfig.selectEmbeddingModelByType()` 方法会：

1. **检查配置**: 读取 `embedding.type` 的值
2. **智能匹配**: 在所有可用的EmbeddingModel Bean中找到对应类型
3. **精确注入**: 将正确的嵌入模型注入到MilvusVectorStore中

```java
switch (embeddingType) {
    case OPENAI:
        // 查找并返回 OpenAiEmbeddingModel
        return embeddingModels.stream()
                .filter(model -> model instanceof OpenAiEmbeddingModel)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("配置为OPENAI但未找到OpenAiEmbeddingModel"));
                
    case ZHIPUAI:
        // 查找并返回 ZhiPuAiEmbeddingModel
        
    case EXTERNAL:
        // 查找并返回 ExternalEmbeddingModelAdapter
}
```

## 🎉 使用效果

✅ **配置 `type: OPENAI`** → VectorStore自动注入 `OpenAiEmbeddingModel`  
✅ **配置 `type: ZHIPUAI`** → VectorStore自动注入 `ZhiPuAiEmbeddingModel`  
✅ **配置 `type: EXTERNAL`** → VectorStore自动注入 `ExternalEmbeddingModelAdapter`

## 🚨 重要提醒

1. **只启用一个服务**: 确保同时只有一个AI服务配置被启用，避免Bean冲突
2. **环境变量**: 确保相应的API Key环境变量已设置
3. **重启应用**: 修改配置后需要重启应用生效

## 🔄 快速切换步骤

1. 修改 `embedding.type` 值
2. 更新对应的AI服务配置  
3. 注释掉其他AI服务配置
4. 重启应用

就这么简单！ 🎯 
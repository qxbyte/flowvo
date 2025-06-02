package org.xue.agents.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.xue.agents.embed.EmbeddingClient;

import java.util.List;

/**
 * 向量存储配置类
 * 根据embedding配置选择不同的EmbeddingModel
 * 
 * VectorStore使用Spring AI自动配置的Milvus实例
 * 只有在EXTERNAL模式时才覆盖EmbeddingModel Bean
 */
@Slf4j
@Configuration
public class VectorStoreConfig {
    
    private final EmbeddingConfig embeddingConfig;
    
    public VectorStoreConfig(EmbeddingConfig embeddingConfig) {
        this.embeddingConfig = embeddingConfig;
    }
    
    /**
     * 只在EXTERNAL模式时创建自定义EmbeddingModel
     * SPRING_AI模式使用Spring AI的自动配置
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "embedding.type", havingValue = "EXTERNAL")
    public EmbeddingModel embeddingModel(EmbeddingClient embeddingClient) {
        log.info("使用外部嵌入模型服务: {}", embeddingConfig.getExternal().getUrl());
        return new ExternalEmbeddingModelAdapter(embeddingClient);
    }

    /**
     * 外部嵌入模型的Spring AI适配器
     * 实现EmbeddingModel接口，将EmbeddingClient的调用适配为Spring AI标准
     */
    private record ExternalEmbeddingModelAdapter(EmbeddingClient embeddingClient) implements EmbeddingModel {

        @Override
        public org.springframework.ai.embedding.EmbeddingResponse call(org.springframework.ai.embedding.EmbeddingRequest request) {
            try {
                List<String> texts = request.getInstructions();
                if (texts.isEmpty()) {
                    throw new IllegalArgumentException("嵌入请求不能为空");
                }

                // 使用外部嵌入服务处理文本
                List<List<Float>> embeddings = embeddingClient.embedBatch(texts);

                // 构建Spring AI的响应
                List<org.springframework.ai.embedding.Embedding> embeddingResults =
                        embeddings.stream()
                                .map(embedding -> {
                                    // 转换List<Float>为float[]
                                    float[] embeddingArray = new float[embedding.size()];
                                    for (int i = 0; i < embedding.size(); i++) {
                                        embeddingArray[i] = embedding.get(i);
                                    }
                                    return new org.springframework.ai.embedding.Embedding(embeddingArray, 0);
                                })
                                .toList();

                return new org.springframework.ai.embedding.EmbeddingResponse(embeddingResults);

            } catch (Exception e) {
                log.error("外部嵌入模型调用失败", e);
                throw new RuntimeException("外部嵌入模型调用失败: " + e.getMessage(), e);
            }
        }

        @Override
        public float[] embed(org.springframework.ai.document.Document document) {
            return embed(document.getText());
        }

        @Override
        public float[] embed(String text) {
            List<Float> result = embeddingClient.embedOne(text);
            float[] floatArray = new float[result.size()];
            for (int i = 0; i < result.size(); i++) {
                floatArray[i] = result.get(i);
            }
            return floatArray;
        }

        @Override
        public List<float[]> embed(List<String> texts) {
            List<List<Float>> results = embeddingClient.embedBatch(texts);
            return results.stream()
                    .map(result -> {
                        float[] floatArray = new float[result.size()];
                        for (int i = 0; i < result.size(); i++) {
                            floatArray[i] = result.get(i);
                        }
                        return floatArray;
                    })
                    .toList();
        }
    }
} 
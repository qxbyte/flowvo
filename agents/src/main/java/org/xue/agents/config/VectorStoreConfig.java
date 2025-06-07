package org.xue.agents.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.ai.zhipuai.ZhiPuAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.xue.agents.embed.EmbeddingClient;

import java.util.List;

/**
 * 向量存储配置类
 * 
 * 根据embedding.type配置精确选择嵌入模型：
 * - embedding.type=OPENAI -> 使用OpenAiEmbeddingModel
 * - embedding.type=ZHIPUAI -> 使用ZhiPuAiEmbeddingModel  
 * - embedding.type=EXTERNAL -> 使用自定义ExternalEmbeddingModelAdapter
 */
@Slf4j
@Configuration
public class VectorStoreConfig {

    private final EmbeddingConfig embeddingConfig;

    @Value("${milvus.host:localhost}")
    private String milvusHost;

    @Value("${milvus.port:19530}")
    private Integer milvusPort;

    @Value("${milvus.username:root}")
    private String milvusUsername;

    @Value("${milvus.password:milvus}")
    private String milvusPassword;

    public VectorStoreConfig(EmbeddingConfig embeddingConfig) {
        this.embeddingConfig = embeddingConfig;
    }

    /**
     * Milvus客户端配置
     */
    @Bean
    public MilvusServiceClient milvusServiceClient() {
        log.info("配置Milvus连接: {}:{}, 用户: {}", milvusHost, milvusPort, milvusUsername);
        
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost(milvusHost)
                .withPort(milvusPort)
                .withAuthorization(milvusUsername, milvusPassword)
                .build();
        
        return new MilvusServiceClient(connectParam);
    }

    /**
     * 外部嵌入模型适配器（当embedding.type=EXTERNAL时使用）
     */
    @Bean
    @ConditionalOnProperty(name = "embedding.type", havingValue = "EXTERNAL")
    public EmbeddingModel externalEmbeddingModel(EmbeddingClient embeddingClient) {
        log.info("创建外部嵌入模型适配器");
        return new ExternalEmbeddingModelAdapter(embeddingClient);
    }

    /**
     * 指定OpenAI EmbeddingModel为Primary，解决Spring AI自动配置的Bean冲突
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "embedding.type", havingValue = "OPENAI")
    public EmbeddingModel primaryOpenAiEmbeddingModel(
            org.springframework.ai.openai.OpenAiEmbeddingModel openAiEmbeddingModel) {
        log.info("设置OpenAI EmbeddingModel为Primary");
        return openAiEmbeddingModel;
    }

    /**
     * 根据embedding.type配置选择嵌入模型的VectorStore
     * 精确匹配配置类型，确保使用正确的嵌入模型
     */
    @Bean("customVectorStore")
    @Primary
    public MilvusVectorStore customVectorStore(MilvusServiceClient milvusServiceClient,
                                        List<EmbeddingModel> embeddingModels) {
        
        EmbeddingConfig.ServiceType embeddingType = embeddingConfig.getType();
        log.info("根据配置选择嵌入模型，embedding.type: {}", embeddingType);
        
        EmbeddingModel selectedModel = selectEmbeddingModelByType(embeddingModels, embeddingType);
        
        log.info("选中的嵌入模型: {}", selectedModel.getClass().getSimpleName());
        
        // 使用MilvusVectorStore的builder方法
        MilvusVectorStore.Builder builder = MilvusVectorStore.builder(milvusServiceClient, selectedModel)
				.databaseName("default")
				.indexType(IndexType.IVF_FLAT)
				.metricType(MetricType.COSINE)
				.batchingStrategy(new TokenCountBatchingStrategy())
				.initializeSchema(true);

        switch (embeddingType) {
            case OPENAI:
                builder.collectionName("doc_chunk");
                builder.embeddingDimension(1536);
                break;

            case ZHIPUAI:
                builder.collectionName("doc_chunk_2048");
                builder.embeddingDimension(2048);
                break;

            case EXTERNAL:
                builder.collectionName("doc_chunk_768");
                builder.embeddingDimension(768);
                break;

            default:
                break;
        }

        return builder.build();
    }

    /**
     * 根据embedding.type配置精确选择嵌入模型
     */
    private EmbeddingModel selectEmbeddingModelByType(List<EmbeddingModel> embeddingModels, EmbeddingConfig.ServiceType embeddingType) {
        log.info("可用的嵌入模型数量: {}, 目标类型: {}", embeddingModels.size(), embeddingType);
        
        // 根据配置类型选择对应的嵌入模型
        switch (embeddingType) {
            case OPENAI:
                return embeddingModels.stream()
                        .filter(model -> model instanceof OpenAiEmbeddingModel)
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("配置为OPENAI但未找到OpenAiEmbeddingModel，请检查OpenAI配置"));
                        
            case ZHIPUAI:
                return embeddingModels.stream()
                        .filter(model -> model instanceof ZhiPuAiEmbeddingModel)
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("配置为ZHIPUAI但未找到ZhiPuAiEmbeddingModel，请检查ZhiPuAI配置"));
                        
            case EXTERNAL:
                return embeddingModels.stream()
                        .filter(model -> model instanceof ExternalEmbeddingModelAdapter)
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("配置为EXTERNAL但未找到ExternalEmbeddingModelAdapter，请检查外部服务配置"));
                        
            default:
                log.warn("未知的嵌入模型类型: {}，使用第一个可用模型", embeddingType);
                if (embeddingModels.isEmpty()) {
                    throw new RuntimeException("没有可用的嵌入模型");
                }
                return embeddingModels.get(0);
        }
    }

    /**
     * 外部嵌入模型适配器
     * 将外部Python服务适配为Spring AI的EmbeddingModel接口
     */
    public static class ExternalEmbeddingModelAdapter implements EmbeddingModel {
        
        private final EmbeddingClient embeddingClient;
        
        public ExternalEmbeddingModelAdapter(EmbeddingClient embeddingClient) {
            this.embeddingClient = embeddingClient;
        }
        
        @Override
        public float[] embed(String text) {
            try {
                List<Float> embedding = embeddingClient.embedOne(text);
                float[] result = new float[embedding.size()];
                for (int i = 0; i < embedding.size(); i++) {
                    result[i] = embedding.get(i);
                }
                return result;
            } catch (Exception e) {
                throw new RuntimeException("外部嵌入服务调用失败", e);
            }
        }
        
        @Override
        public float[] embed(Document document) {
            return embed(document.getText());
        }
        
        @Override
        public EmbeddingResponse call(org.springframework.ai.embedding.EmbeddingRequest request) {
            // 简化实现，实际项目中需要完整实现
            throw new UnsupportedOperationException("请使用embed()方法");
        }
        
        @Override
        public int dimensions() {
            return 768; // 外部服务的维度
        }
    }
} 
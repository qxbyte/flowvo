package org.xue.agents.embed;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.xue.agents.config.EmbeddingConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 外部向量化服务客户端
 * 仅在配置为EXTERNAL模式时启用
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "embedding", name = "type", havingValue = "EXTERNAL")
public class EmbeddingClient {

    private final String baseUrl;
    private final RestTemplate restTemplate;

    @Autowired
    public EmbeddingClient(RestTemplate restTemplate, EmbeddingConfig embeddingConfig) {
        this.restTemplate = restTemplate;
        this.baseUrl = embeddingConfig.getExternal().getUrl();
        log.info("外部向量化服务客户端已启用，服务地址: {}", baseUrl);
    }

    // 1. 文本切分
    public List<String> split(String text) {
        Map<String, String> req = new HashMap<>();
        req.put("text", text);
        
        try {
            ResponseEntity<Map> resp = restTemplate.postForEntity(baseUrl + "/split", req, Map.class);
            return (List<String>) resp.getBody().get("chunks");
        } catch (Exception e) {
            log.error("外部文本切分服务调用失败: {}", e.getMessage());
            throw new RuntimeException("文本切分失败", e);
        }
    }

    // 2. 单条文本向量化
    public List<Float> embedOne(String text) {
        Map<String, String> req = new HashMap<>();
        req.put("text", text);
        
        try {
            ResponseEntity<Map> resp = restTemplate.postForEntity(baseUrl + "/embed_one", req, Map.class);
            List<?> emb = (List<?>) resp.getBody().get("embedding");
            List<Float> result = new ArrayList<>();
            for (Object o : emb) result.add(Float.valueOf(o.toString()));
            return result;
        } catch (Exception e) {
            log.error("外部向量化服务调用失败: {}", e.getMessage());
            throw new RuntimeException("文本向量化失败", e);
        }
    }

    // 3. 批量文本向量化
    public List<List<Float>> embedBatch(List<String> texts) {
        Map<String, Object> req = new HashMap<>();
        req.put("texts", texts);
        
        try {
            ResponseEntity<Map> resp = restTemplate.postForEntity(baseUrl + "/embed_batch", req, Map.class);
            List<?> arr = (List<?>) resp.getBody().get("embeddings");
            List<List<Float>> result = new ArrayList<>();
            for (Object o : arr) {
                List<?> emb = (List<?>) o;
                List<Float> one = new ArrayList<>();
                for (Object x : emb) one.add(Float.valueOf(x.toString()));
                result.add(one);
            }
            return result;
        } catch (Exception e) {
            log.error("外部批量向量化服务调用失败: {}", e.getMessage());
            throw new RuntimeException("批量向量化失败", e);
        }
    }

    // 4. 文档切分+向量化
    public List<ChunkEmbedding> splitEmbed(String docText) {
        Map<String, String> req = new HashMap<>();
        req.put("text", docText);
        
        try {
            ResponseEntity<Map> resp = restTemplate.postForEntity(baseUrl + "/split_embed", req, Map.class);
            // 适配外部服务的实际响应格式：{"results": [...], "total_chunks": 1, "original_text_length": 8}
            List<Map<String, Object>> arr = (List<Map<String, Object>>) resp.getBody().get("chunk_embeddings");
            List<ChunkEmbedding> result = new ArrayList<>();
            for (Map<String, Object> map : arr) {
                String chunk = map.get("chunk").toString();
                List<Float> emb = new ArrayList<>();
                for (Object o : (List<?>) map.get("embedding")) emb.add(Float.valueOf(o.toString()));
                result.add(new ChunkEmbedding(chunk, emb));
            }
            return result;
        } catch (Exception e) {
            log.error("外部文档切分向量化服务调用失败: {}", e.getMessage());
            throw new RuntimeException("文档向量化失败", e);
        }
    }

    // 内部实体类
    @Data
    public static class ChunkEmbedding {
        public String chunk;
        public List<Float> embedding;
        
        public ChunkEmbedding(String chunk, List<Float> embedding) {
            this.chunk = chunk;
            this.embedding = embedding;
        }
        
        @Override
        public String toString() {
            return "ChunkEmbedding{" + "chunk='" + chunk + '\'' + ", embedding(size)=" + embedding.size() + '}';
        }
    }
} 
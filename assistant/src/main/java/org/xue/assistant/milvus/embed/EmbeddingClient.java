package org.xue.assistant.milvus.embed;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.reflections.Reflections.log;

@Component
@Lazy
public class EmbeddingClient {

    private final String baseUrl;
    private final RestTemplate restTemplate;

    @Autowired
    public EmbeddingClient(@Value("${local.embedding.server}") String baseUrl, RestTemplate restTemplate) {
        this.baseUrl = baseUrl;
        this.restTemplate = restTemplate;
    }

    // 1. 文本切分
    public List<String> split(String text) {
        Map<String, String> req = new HashMap<>();
        req.put("text", text);

        try {
            ResponseEntity<Map> resp = restTemplate.postForEntity(baseUrl + "/split", req, Map.class);
            return (List<String>) resp.getBody().get("chunks");
        } catch (ResourceAccessException e) {
            log.warn("嵌入服务连接失败", e);
            return Collections.emptyList();
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
        } catch (ResourceAccessException e) {
            log.warn("嵌入服务连接失败", e);
            return Collections.emptyList();
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
        } catch (ResourceAccessException e) {
            log.warn("嵌入服务连接失败", e);
            return Collections.emptyList();
        }


    }

    // 4. 文档切分+向量化
    public List<ChunkEmbedding> splitEmbed(String docText) {
        Map<String, String> req = new HashMap<>();
        req.put("text", docText);

        try {
            ResponseEntity<Map> resp = restTemplate.postForEntity(baseUrl + "/split_embed", req, Map.class);
            List<Map<String, Object>> arr = (List<Map<String, Object>>) resp.getBody().get("chunk_embeddings");
            List<ChunkEmbedding> result = new ArrayList<>();
            for (Map<String, Object> map : arr) {
                String chunk = map.get("chunk").toString();
                List<Float> emb = new ArrayList<>();
                for (Object o : (List<?>) map.get("embedding")) emb.add(Float.valueOf(o.toString()));
                result.add(new ChunkEmbedding(chunk, emb));
            }
            return result;
        } catch (ResourceAccessException e) {
            log.warn("嵌入服务连接失败", e);
            return Collections.emptyList();
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

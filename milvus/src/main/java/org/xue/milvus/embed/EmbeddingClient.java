package org.xue.milvus.embed;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
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
        ResponseEntity<Map> resp = restTemplate.postForEntity(baseUrl + "/split", req, Map.class);
        return (List<String>) resp.getBody().get("chunks");
    }

    // 2. 单条文本向量化
    public List<Double> embedOne(String text) {
        Map<String, String> req = new HashMap<>();
        req.put("text", text);
        ResponseEntity<Map> resp = restTemplate.postForEntity(baseUrl + "/embed_one", req, Map.class);
        List<?> emb = (List<?>) resp.getBody().get("embedding");
        List<Double> result = new ArrayList<>();
        for (Object o : emb) result.add(Double.valueOf(o.toString()));
        return result;
    }

    // 3. 批量文本向量化
    public List<List<Double>> embedBatch(List<String> texts) {
        Map<String, Object> req = new HashMap<>();
        req.put("texts", texts);
        ResponseEntity<Map> resp = restTemplate.postForEntity(baseUrl + "/embed_batch", req, Map.class);
        List<?> arr = (List<?>) resp.getBody().get("embeddings");
        List<List<Double>> result = new ArrayList<>();
        for (Object o : arr) {
            List<?> emb = (List<?>) o;
            List<Double> one = new ArrayList<>();
            for (Object x : emb) one.add(Double.valueOf(x.toString()));
            result.add(one);
        }
        return result;
    }

    // 4. 文档切分+向量化
    public List<ChunkEmbedding> splitEmbed(String docText) {
        Map<String, String> req = new HashMap<>();
        req.put("text", docText);
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

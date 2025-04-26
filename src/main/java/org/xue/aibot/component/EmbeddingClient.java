package org.xue.aibot.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class EmbeddingClient {

    private final RestTemplate restTemplate;

    public EmbeddingClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${local.embedding.server}")
    private String url;

    public List<Double> embed(String Text) {
        Map<String, Object> req = Map.of("input", List.of(Text));
        Map result = restTemplate.postForObject(url, req, Map.class);
        List<Double> embeddings = (List<Double>) result.get("embeddings");
        return embeddings;
    }

    public List<List<Double>> embedBatch(String Text) {
        Map<String, List<String>> req = Map.of("input", List.of("你好，世界", "hello world"));
        Map result = restTemplate.postForObject(url, req, Map.class);
        List<List<Double>> embeddings = (List<List<Double>>) result.get("embeddings");
        return embeddings;
    }

//    public List<Double> embedOpenAI(String Text) {
//        Map<String, Object> req = Map.of("input", List.of(Text));
//        Map result = restTemplate.postForObject(url, req, Map.class);
//        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("embeddings");
//        List<Double> embedding = (List<Double>) data.get(0).get("embedding");
//        return embedding;
//    }
}

package org.xue.aibot.service.impl;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xue.aibot.service.QdrantService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QdrantServiceImpl implements QdrantService {

    @Autowired
    private QdrantVectorStore qdrantVectorStore;

    // 写入
    @Override
    public void saveText(String text, String docId, int chunkIndex) {
        Document doc = new Document(text, Map.of("docId", docId, "chunkIndex", chunkIndex));
        qdrantVectorStore.add(List.of(doc));
    }

    // 检索
    @Override
    public List<String> searchSimilar(String query, int topK) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build();
        List<Document> results = qdrantVectorStore.similaritySearch(request);
        return results.stream()
                .map(Document::getText)
                .collect(Collectors.toList());
    }

    // 删除（只能按id批量删，不能直接按metadata删）
    @Override
    public void deleteByDocId(String docId) {
        // 先查所有，找到目标docId的id
        // 或设足够大的topK
        SearchRequest request = SearchRequest.builder()
                .query(null)
                .topK(1000)
                .build();
        List<Document> docs = qdrantVectorStore.similaritySearch(request);
        List<String> toDeleteIds = docs.stream()
                .filter(d -> docId.equals(d.getMetadata().get("docId")))
                .map(Document::getId)
                .collect(Collectors.toList());
        qdrantVectorStore.delete(toDeleteIds);
    }
}


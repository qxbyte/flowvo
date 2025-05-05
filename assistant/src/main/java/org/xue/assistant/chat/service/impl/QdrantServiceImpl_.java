package org.xue.assistant.chat.service.impl;


import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xue.assistant.chat.service.QdrantService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QdrantServiceImpl_ implements QdrantService {

    private final QdrantVectorStore qdrantVectorStore;

    public QdrantServiceImpl_(QdrantVectorStore qdrantVectorStore) {
        this.qdrantVectorStore = qdrantVectorStore;
    }

    @Autowired
    private EmbeddingModel embeddingModel; // OpenAiEmbeddingClient, HuggingFaceEmbeddingClient等

    // 写入
    @Override
    public void saveText(String text, String docId, int chunkIndex) {


        /**
         *
         *
         */
        // 1. 用 EmbeddingModel 生成向量
//        EmbeddingResponse embeddingResponse = this.embeddingModel.embedForResponse(List.of(text));
//        float[] embedding = embeddingResponse.getResult().getOutput(); // 取第一个文本的向量（float[]）
//
//        // 2. 将 float[] 转成 List<Double>（QdrantVectorStore 需要的是 List<Double>）
//        List<Double> vector = new ArrayList<>();
//        for (float v : embedding) {
//            vector.add((double) v);
//        }



        // 3. 构造 Document（带向量、元数据、原文）
        Map<String, Object> metadata = Map.of(
                "docId", docId,
                "chunkIndex", chunkIndex
        );
        Document doc = new Document(text, metadata);

        // 4. 写入 Qdrant
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


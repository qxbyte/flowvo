//package org.xue.aibot.service.impl;
//
//import org.springframework.ai.document.Document;
//import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
//import org.springframework.ai.vectorstore.SearchRequest;
//import org.springframework.ai.vectorstore.filter.Filter;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.xue.aibot.service.QdrantService;
//
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Service
//public class QdrantServiceImpl implements QdrantService {
//
//    @Autowired
//    private QdrantVectorStore qdrantVectorStore;
//
//    // 写入
//    @Override
//    public void saveText(String text, String docId, int chunkIndex) {
//        Document doc = new Document(text, Map.of("docId", docId, "chunkIndex", chunkIndex));
//        qdrantVectorStore.add(List.of(doc));
//    }
//
//    // 检索
//    @Override
//    public List<String> searchSimilar(String query, int topK) {
//        SearchRequest request = SearchRequest.query(query)
//                .withTopK(topK);
//        List<Document> results = qdrantVectorStore.similaritySearch(request);
//        return results.stream()
//                .map(Document::getContent)
//                .collect(Collectors.toList());
//    }
//
//    // 删除
//    @Override
//    public void deleteByDocId(String docId) {
//        Filter filter = Filter.builder().eq("docId", docId).build();
//        qdrantVectorStore.delete(filter);
//    }
//}

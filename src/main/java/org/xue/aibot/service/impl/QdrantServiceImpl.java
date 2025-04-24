package org.xue.aibot.service.impl;

import org.xue.aibot.service.QdrantService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QdrantServiceImpl implements QdrantService {

    @Autowired
    private QdrantEmbeddingClient qdrantEmbeddingClient;

    @Autowired
    private OpenAiEmbeddingClient openAiEmbeddingClient;

    @Override
    public void saveText(String text, String docId, int chunkIndex) {
        Embedding embedding = openAiEmbeddingClient.embed(text);
        QdrantEmbeddingDocument doc = QdrantEmbeddingDocument.builder()
                .content(text)
                .metadata(Map.of("docId", docId, "chunkIndex", chunkIndex))
                .embedding(embedding)
                .build();
        qdrantEmbeddingClient.add(List.of(doc));
    }

    @Override
    public List<String> searchSimilar(String query, int topK) {
        Embedding queryEmbedding = openAiEmbeddingClient.embed(query);
        List<QdrantEmbeddingDocument> docs = qdrantEmbeddingClient.similaritySearch(queryEmbedding, topK);
        return docs.stream().map(QdrantEmbeddingDocument::getContent).collect(Collectors.toList());
    }

    @Override
    public void deleteByDocId(String docId) {
        qdrantEmbeddingClient.deleteByMetadata(Map.of("docId", docId));
    }
}

package org.xue.core.milvus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xue.core.milvus.embed.EmbeddingClient;
import org.xue.core.milvus.service.ChunkMilvusService;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class EmbeddingClientTest {

    @Autowired
    private EmbeddingClient embeddingClient;

    @Test
    public void testSplit() {
        String doc = "章节一：这是第一段内容。\n章节二：这是第二段内容。";
        List<String> chunks = embeddingClient.split(doc);
        System.out.println("切分结果: " + chunks);
    }

    @Test
    public void testEmbedOne() {
//        String text = "录鬼簿";
        String text = "增值税系统";

        List<Float> embedding = embeddingClient.embedOne(text);
        System.out.println("单段文本embedding: " + embedding);
    }

    @Test
    public void testEmbedBatch() {
        List<String> texts = Arrays.asList("段落一", "段落二", "段落三");
        List<List<Float>> embeddings = embeddingClient.embedBatch(texts);
        System.out.println("批量embedding: " + embeddings);
    }

    @Autowired
    private ChunkMilvusService chunkMilvusService;

    @Test
    public void testSplitEmbed() {
        String doc = "增值税系统";
        List<EmbeddingClient.ChunkEmbedding> chunkEmbeddings = embeddingClient.splitEmbed(doc);
        for (EmbeddingClient.ChunkEmbedding ce : chunkEmbeddings) {
            System.out.println("chunk: " + ce.getChunk());
            System.out.println("embedding: " + ce.getEmbedding());
        }
    }
}

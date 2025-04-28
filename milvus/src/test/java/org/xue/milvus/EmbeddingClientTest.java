package org.xue.milvus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xue.milvus.embed.EmbeddingClient;
import org.xue.milvus.service.ChunkMilvusService;

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
        String text = "明朝嘉靖元年，《三国志通俗演义》刊刻而成，题“晋平阳侯陈寿史传，后学罗贯中编次”，这就是后来《三国演义》各种版本的祖本。《三国志演义》与《三国演义》，都不是罗贯中原作的名称，而是在小说流传的过程中出现的。前者见于明朝周弘祖的《古今书刻》，相沿已久；后者则见于清朝毛宗岗的《读三国志法》。它们各自从一定角度反映了《三国》的特点。";

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
        String doc = "录鬼簿";
        List<EmbeddingClient.ChunkEmbedding> chunkEmbeddings = embeddingClient.splitEmbed(doc);
        for (EmbeddingClient.ChunkEmbedding ce : chunkEmbeddings) {
            System.out.println("chunk: " + ce.getChunk());
            System.out.println("embedding: " + ce.getEmbedding());
        }
    }
}

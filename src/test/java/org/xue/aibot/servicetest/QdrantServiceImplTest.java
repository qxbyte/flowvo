package org.xue.aibot.servicetest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xue.aibot.component.EmbeddingClient;
import org.xue.aibot.service.QdrantService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class QdrantServiceImplTest {

    @Autowired
    private QdrantService qdrantService;

    @Autowired
    private EmbeddingClient EmbeddingClient;

    @Test
    void testSaveAndSearchAndDelete() {
        String text = "Spring AI Qdrant 向量库测试";
        String docId = "test_doc_001";
        int chunkIndex = 1;

        // 1. 写入
        qdrantService.saveText(text, docId, chunkIndex);

        // 2. 检索
        List<String> results = qdrantService.searchSimilar("Qdrant 测试", 5);
        System.out.println("检索结果: " + results);

        assertThat(results).isNotEmpty();
        assertThat(results.get(0)).contains("Qdrant");

        // 3. 删除
        qdrantService.deleteByDocId(docId);

        // 4. 检索确认已删除
        List<String> resultsAfterDelete = qdrantService.searchSimilar("Qdrant 测试", 5);
        System.out.println("删除后检索: " + resultsAfterDelete);
    }



        @Test
        void embeddingTest() {
            List<Double> l = EmbeddingClient.embed("你好");
            System.out.println("向量: " + l);
        }

    @Test
    void embeddingBatchTest() {
        List<List<Double>> l = EmbeddingClient.embedBatch("你好");
        System.out.println("向量: " + l);
    }
}

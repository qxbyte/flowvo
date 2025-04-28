package org.xue.milvus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xue.milvus.embed.EmbeddingClient;
import org.xue.milvus.service.MilvusService;

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
        String text = "这是一段需要生成embedding的文本";
        List<Double> embedding = embeddingClient.embedOne(text);
        System.out.println("单段文本embedding: " + embedding);
    }

    @Test
    public void testEmbedBatch() {
        List<String> texts = Arrays.asList("段落一", "段落二", "段落三");
        List<List<Double>> embeddings = embeddingClient.embedBatch(texts);
        System.out.println("批量embedding: " + embeddings);
    }

    @Autowired
    private MilvusService milvusService;

    @Test
    public void testSplitEmbed() {
        String doc = "评书《三国演义》是袁阔成创作的长篇评书，共365回。主要讲述东汉末年，黄巾起义，天下大乱，董卓废少帝，拥立献帝，独掌朝政。曹操大败袁绍，统一北方。刘备三顾茅庐，终得诸葛亮出山相助，遂联合东吴孙权，形成了三分天下局面。孙权为夺回荆州，与曹操结盟。\\n \" +\n" +
                "                \"诸葛亮六出祁山欲收复中原，最终病殁五丈原。刘禅软弱，终为司马氏所灭。西晋灭吴，三国归于一统。评书《三国演义》以魏、蜀、吴三国的兴亡为线索，描绘了汉末至晋统一的一百年间历史，描述了魏、蜀、吴三国之间的政治和军事斗争，对当时动乱的社会状况有所反映，塑造了刘备、诸葛亮、曹操、孙权、周瑜、关羽、张飞等众多的人物。表现出鲜明的儒家仁政思想。同时也遣责了军阀混战及暴君的苛政，寄托了人民渴求明君仁政、社会安定的愿望。\\n\" +\n" +
                "                \"袁阔成以独特的视角，对历史事件和历史人物进行演义和评说。评书《三国演义》在艺术结构、人物命运和重要事件的表述和处理，因为说书人艺术功力的不同，表现上也有所不同。袁阔成的评书《三国演义》，吸收南北评书评话的众家优长，形成独家特色，得到广泛的好评。\\n \" +\n" +
                "                \"二十世纪八十年代，袁阔成在营口广播电台编辑李程和中央人民广播电台编辑袁枫等人的帮助下，查阅了大量资料，也与当时人称“滑稽三国”的施星夔、评话演员康重华、唐耿良以及一些研究三国历史的专家学者进行了探讨，并亲自到三国古战场和当时的名城重镇实地参观。\\n \" +\n" +
                "                \"这本书我是从七岁就看到了，以后又看了不知有多少次，十一二岁时看到“关公”死后，就扔下了；十四五岁时，看到诸葛亮死后又扔下了。一直到大学时代才勉强把全书看完。没想到袁阔成的说书《三国演义》又“演义”了一番，还演得真好！人物性格都没走样，而且十分生动有趣，因此我从“话说天下大势合久必分，分久必合”一直听到“三分归一统”，连我从前认为没有什么趣味的“入西川二士争功”，也显得波澜壮阔。我觉得能成为一位“好”的说书者，也真不容易。（冰心 评）";
//        List<EmbeddingClient.ChunkEmbedding> chunkEmbeddings = embeddingClient.splitEmbed(doc);
//        for (EmbeddingClient.ChunkEmbedding ce : chunkEmbeddings) {
//            System.out.println("chunk: " + ce.getChunk());
//            System.out.println("embedding: " + ce.getEmbedding());
//        }
        milvusService.insertChunks(doc);

    }
}

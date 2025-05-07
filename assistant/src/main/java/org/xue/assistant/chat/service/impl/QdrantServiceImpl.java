package org.xue.assistant.chat.service.impl;//package org.xue.aibot.service.impl;
//
//import io.qdrant.client.QdrantClient;
//import io.qdrant.client.http.model.Points;
//import io.qdrant.client.http.model.Points.PointStruct;
//import io.qdrant.client.http.model.Points.UpsertPoints;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class QdrantServiceImpl {
//
//    private final QdrantClient client;
//    private final String collection;
//
//    public QdrantServiceImpl(QdrantClient client,
//                             @Value("${ai.vectorstore.qdrant.collection}") String collection) {
//        this.client = client;
//        this.collection = collection;
//    }
//
//    /**
//     * @param docId    向量在 Qdrant 中的 ID（自己保证唯一）
//     * @param vector   从本地 embedding 服务拿到的 List<Float>
//     * @param metadata 其他想绑定的 metadata
//     */
//    public void upsertVector(String docId,
//                             List<Float> vector,
//                             Map<String,Object> metadata) {
//
//        // 1) 构造要写入的点
//        PointStruct point = PointStruct.builder()
//                .id(docId)
//                .vector(vector)
//                .payload(metadata)
//                .build();
//
//        // 2) 构造批量 upsert 请求
//        UpsertPoints upsert = UpsertPoints.builder()
//                .collectionName(collection)
//                .points(List.of(point))
//                .build();
//
//        // 3) 执行写入
//        client.upsert(upsert);
//    }
//}

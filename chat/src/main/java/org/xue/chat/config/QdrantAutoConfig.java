package org.xue.chat.config;
//
//import io.qdrant.client.QdrantGrpcClient;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import io.qdrant.client.QdrantClient;
//
//@Configuration
//public class QdrantAutoConfig {
//
//    @Value("${spring.ai.vectorstore.qdrant.host}")
//    private String host;
//
//    @Value("${spring.ai.vectorstore.qdrant.port}")
//    private int port;
//
//    @Bean
//    public QdrantClient qdrantClient() {
//        // 官方 client 提供了一个 builder API：
//        return new QdrantClient(QdrantGrpcClient.newBuilder(host, port).build());
//    }
//}

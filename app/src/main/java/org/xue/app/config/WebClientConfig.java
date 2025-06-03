//package org.xue.app.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.client.reactive.ReactorClientHttpConnector;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.netty.http.client.HttpClient;
//import reactor.netty.resources.ConnectionProvider;
//
//import java.time.Duration;
//
///**
// * WebClient配置类
// * 用于替代Feign进行服务间调用
// */
//@Configuration
//public class WebClientConfig {
//
//    @Value("${services.agents.url:http://localhost:8081}")
//    private String agentsServiceUrl;
//
//    /**
//     * Agents服务WebClient
//     */
//    @Bean
//    public WebClient agentsWebClient() {
//        // 配置连接池
//        ConnectionProvider connectionProvider = ConnectionProvider.builder("agents-pool")
//                .maxConnections(50)
//                .maxIdleTime(Duration.ofSeconds(30))
//                .maxLifeTime(Duration.ofMinutes(5))
//                .pendingAcquireTimeout(Duration.ofSeconds(60))
//                .evictInBackground(Duration.ofSeconds(120))
//                .build();
//
//        // 配置HttpClient
//        HttpClient httpClient = HttpClient.create(connectionProvider)
//                .responseTimeout(Duration.ofMinutes(5)) // 支持长时间的流式响应
//                .keepAlive(true);
//
//        return WebClient.builder()
//                .baseUrl(agentsServiceUrl)
//                .clientConnector(new ReactorClientHttpConnector(httpClient))
//                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
//                .build();
//    }
//
//    /**
//     * 通用WebClient
//     */
//    @Bean
//    public WebClient webClient() {
//        return WebClient.builder()
//                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
//                .build();
//    }
//}
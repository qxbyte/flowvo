package org.xue.assistant.functioncall.client;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 通用大模型客户端：OpenAI / DeepSeek / Moonshot 等接口统一封装
 * 支持流式响应、非流式响应、函数调用
 */
@Slf4j
@Component
public class OpenAiClient {

    private final String apiKey;
    private final String baseUrl;
    private final OkHttpClient httpClient;

    public OpenAiClient(
            @Value("${ai.openai.api-key}") String apiKey, 
            @Value("${ai.openai.base-url}") String baseUrl,
            @Value("${ai.openai.proxy.enabled:false}") boolean proxyEnabled,
            @Value("${ai.openai.proxy.host:}") String proxyHost,
            @Value("${ai.openai.proxy.port:0}") int proxyPort,
            @Value("${ai.openai.connect-timeout:30}") int connectTimeout,
            @Value("${ai.openai.read-timeout:60}") int readTimeout
    ) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);
        
        // 配置HTTP/2和TLS
        builder.protocols(java.util.Arrays.asList(Protocol.HTTP_1_1, Protocol.HTTP_2));
        
        // 配置自定义连接池，提高连接复用和稳定性
        ConnectionPool connectionPool = new ConnectionPool(5, 30, TimeUnit.SECONDS);
        builder.connectionPool(connectionPool);
        
        // 添加请求重试拦截器
        builder.addInterceptor(chain -> {
            Request request = chain.request();
            Response response = null;
            IOException exception = null;
            int maxRetries = 3;
            
            for (int retry = 0; retry < maxRetries; retry++) {
                try {
                    response = chain.proceed(request);
                    if (response.isSuccessful()) {
                        return response;
                    } else if (response.code() >= 500 || response.code() == 429) {
                        // 服务端错误或限流，需要重试
                        String responseBody = response.body() != null ? response.body().string() : "";
                        log.warn("服务端错误，尝试重试 ({}/{}): code={}, body={}", 
                                retry + 1, maxRetries, response.code(), responseBody);
                        response.close();
                        
                        // 指数退避重试
                        Thread.sleep((retry + 1) * 1000);
                        continue;
                    } else {
                        // 其他错误，不重试
                        return response;
                    }
                } catch (IOException e) {
                    // 网络错误或连接问题，保存异常并重试
                    if (response != null) {
                        response.close();
                    }
                    exception = e;
                    log.warn("请求发生IO异常，尝试重试 ({}/{}): {}", retry + 1, maxRetries, e.getMessage());
                    
                    try {
                        // 指数退避重试
                        Thread.sleep((retry + 1) * 1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("重试被中断", ie);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("重试被中断", e);
                }
            }
            
            // 如果到这里，说明重试次数已用完，但仍未成功
            if (exception != null) {
                throw exception;
            }
            return response;
        });
        
        // 如果启用代理，添加代理配置
        if (proxyEnabled && proxyHost != null && !proxyHost.isEmpty() && proxyPort > 0) {
            log.info("使用代理连接OpenAI: {}:{}", proxyHost, proxyPort);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            builder.proxy(proxy);
        }
        
        this.httpClient = builder.build();
        
        log.info("OpenAI客户端初始化完成，baseUrl={}, 连接超时={}秒, 读取超时={}秒", baseUrl, connectTimeout, readTimeout);
    }

    /**
     * 通用请求：使用策略处理流式或非流式响应
     */
    public void chat(String requestJson, ChatStrategy strategy) throws Exception {
        strategy.execute(requestJson, this);
    }

    public String chatSync(String requestJson) throws Exception {
        Request request = new Request.Builder()
                .url(baseUrl + "/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Connection", "keep-alive")
                .post(RequestBody.create(requestJson, MediaType.parse("application/json")))
                .build();

        log.info("发送同步请求到OpenAI: {}", baseUrl);
        
        int maxRetries = 3;
        int retryCount = 0;
        Exception lastException = null;
        
        while (retryCount < maxRetries) {
            try {
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "无响应体";
                        log.error("OpenAI调用失败: code={}, body={}", response.code(), errorBody);
                        
                        if (response.code() >= 500 || response.code() == 429) {
                            // 服务器错误或请求过多，进行重试
                            retryCount++;
                            if (retryCount < maxRetries) {
                                log.info("服务器错误，{}ms后重试 ({}/{})", retryCount * 1000, retryCount, maxRetries);
                                Thread.sleep(retryCount * 1000); // 指数延迟
                                continue;
                            }
                        }
                        
                        throw new RuntimeException("调用OpenAI失败: " + response.code() + ", " + errorBody);
                    }
                    
                    String responseBody = response.body() != null ? response.body().string() : "";
                    log.info("收到OpenAI响应，长度: {}", responseBody.length());
                    return responseBody;
                }
            } catch (IOException e) {
                lastException = e;
                log.error("OpenAI请求发送失败 (尝试 {}/{}): {}", retryCount + 1, maxRetries, e.getMessage(), e);
                
                // 对特定错误进行特殊处理
                if (e.getMessage() != null && 
                    (e.getMessage().contains("SETTINGS") || 
                     e.getMessage().contains("Connection reset") ||
                     e.getMessage().contains("timeout"))) {
                    log.warn("检测到HTTP/2协议错误或连接重置，尝试重试");
                    retryCount++;
                    if (retryCount < maxRetries) {
                        // 指数退避策略
                        int sleepTime = retryCount * 2000; // 2秒, 4秒, 6秒...
                        log.info("{}ms后重试 ({}/{})", sleepTime, retryCount, maxRetries);
                        Thread.sleep(sleepTime);
                        continue;
                    }
                }
                
                throw e;
            } catch (Exception e) {
                lastException = e;
                log.error("处理OpenAI响应时出错: {}", e.getMessage(), e);
                
                retryCount++;
                if (retryCount < maxRetries) {
                    log.info("{}ms后重试 ({}/{})", retryCount * 1500, retryCount, maxRetries);
                    Thread.sleep(retryCount * 1500); // 1.5秒, 3秒, 4.5秒...
                    continue;
                }
                
                throw e;
            }
        }
        
        // 如果到达这里，说明重试次数用完还是失败
        if (lastException != null) {
            throw lastException;
        }
        
        throw new RuntimeException("OpenAI请求失败，重试次数已用完");
    }

    public void chatStream(String requestJson) {
        Request request = new Request.Builder()
                .url(baseUrl + "/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "text/event-stream")
                .addHeader("Connection", "keep-alive")
                .post(RequestBody.create(requestJson, MediaType.parse("application/json")))
                .build();

        log.info("发送流式请求到OpenAI: {}", baseUrl);
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.error("流式请求失败: {}", e.getMessage(), e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String payload = line.substring(6).trim();
                            if ("[DONE]".equals(payload)) break;
                            String content = extractDeltaContent(payload);
                            if (content != null) log.info(content);
                        }
                    }
                    log.info("\n\n结束时间: {}", LocalDateTime.now());
                } catch (Exception e) {
                    log.error("响应流解析错误: {}", e.getMessage(), e);
                }
            }
        });
    }

    private String extractDeltaContent(String jsonLine) {
        try {
            int start = jsonLine.indexOf("\"content\":\"");
            if (start == -1) return null;
            start += 11;
            int end = jsonLine.indexOf("\"", start);
            return jsonLine.substring(start, end);
        } catch (Exception e) {
            return null;
        }
    }

    public interface ChatStrategy {
        void execute(String requestJson, OpenAiClient client) throws Exception;
    }

    public static class SyncChatStrategy implements ChatStrategy {
        @Override
        public void execute(String requestJson, OpenAiClient client) throws Exception {
            String result = client.chatSync(requestJson);
            log.info("\n非流式响应:\n{}", result);
        }
    }

    public static class StreamChatStrategy implements ChatStrategy {
        @Override
        public void execute(String requestJson, OpenAiClient client) {
            client.chatStream(requestJson);
        }
    }
}
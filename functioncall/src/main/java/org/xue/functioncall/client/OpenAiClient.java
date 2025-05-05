package org.xue.functioncall.client;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

    public OpenAiClient(@Value("${spring.ai.openai.api-key}") String apiKey, @Value("${spring.ai.openai.base-url}") String baseUrl) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();
    }

    /**
     * 通用请求：使用策略处理流式或非流式响应
     */
    public void chat(String requestJson, ChatStrategy strategy) throws Exception {
        strategy.execute(requestJson, this);
    }

    public String chatSync(String requestJson) throws Exception {
        Request request = new Request.Builder()
                .url(baseUrl + "/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestJson, MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("调用失败: " + response.code() + ", " + response.body().string());
            }
            return response.body().string();
        }
    }

    public void chatStream(String requestJson) {
        Request request = new Request.Builder()
                .url(baseUrl + "/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestJson, MediaType.parse("application/json")))
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.err.println("请求失败: " + e.getMessage());
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
                    log.error("响应流解析错误: {}", e.getMessage());
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
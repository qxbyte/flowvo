package org.xue.app.chat.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.xue.app.chat.service.ImageProcessingService;
import org.xue.app.chat.service.ModelConfigService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

/**
 * OpenAI Vision API 客户端
 * 负责调用 OpenAI 的图像识别接口
 */
@Component
@Slf4j
public class OpenAIVisionClient {
    
    // 使用现有的ai.openai配置
    @Value("${ai.openai.api-key:}")
    private String openaiApiKey;
    
    @Value("${ai.openai.base-url:https://api.openai.com}")
    private String openaiBaseUrl;
    
    // 使用现有的ai.deepseek配置（虽然DeepSeek不支持Vision，但保留接口一致性）
    @Value("${ai.deepseek.api-key:}")
    private String deepseekApiKey;
    
    @Value("${ai.deepseek.base-url:https://api.deepseek.com}")
    private String deepseekBaseUrl;
    
    // 代理配置
    @Value("${ai.openai.proxy.enabled:false}")
    private boolean proxyEnabled;
    
    @Value("${ai.openai.proxy.host:127.0.0.1}")
    private String proxyHost;
    
    @Value("${ai.openai.proxy.port:7890}")
    private int proxyPort;
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ImageProcessingService imageProcessingService;
    private final ModelConfigService modelConfigService;
    
    public OpenAIVisionClient(ImageProcessingService imageProcessingService, 
                             ModelConfigService modelConfigService) {
        this.imageProcessingService = imageProcessingService;
        this.modelConfigService = modelConfigService;
        this.objectMapper = new ObjectMapper();
        
        // 构建HTTP客户端，支持代理
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS);
        
        // 如果启用代理，则配置代理
        if (proxyEnabled) {
            builder.proxy(new Proxy(Proxy.Type.HTTP, 
                new InetSocketAddress(proxyHost, proxyPort)));
            log.info("启用代理: {}:{}", proxyHost, proxyPort);
        }
        
        this.httpClient = builder.build();
    }
    
    /**
     * 调用 Vision API 进行图像识别
     */
    public String recognizeImage(MultipartFile imageFile, String userMessage, String model) throws IOException {
        // 根据模型获取对应的配置
        String provider = modelConfigService.getProviderForModel(model);
        
        if ("deepseek".equals(provider)) {
            throw new IllegalArgumentException("DeepSeek 模型不支持图像识别功能，请选择OpenAI的Vision模型（如gpt-4o-mini, gpt-4o等）");
        }
        
        // 检查模型是否支持Vision
        if (!modelConfigService.isVisionSupported(model)) {
            throw new IllegalArgumentException("模型 " + model + " 不支持图像识别功能");
        }
        
        String apiKey = openaiApiKey;
        String baseUrl = openaiBaseUrl;
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("OpenAI API Key 未配置，请在配置文件中设置 ai.openai.api-key");
        }
        
        // 构建JSON请求体
        String requestBody = buildVisionRequestJson(imageFile, userMessage, model);
        
        // 构建HTTP请求
        Request request = new Request.Builder()
            .url(baseUrl + "/v1/chat/completions")
            .addHeader("Authorization", "Bearer " + apiKey)
            .addHeader("Content-Type", "application/json")
            .addHeader("User-Agent", "FlowvoApp/1.0")
            .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
            .build();
        
        log.info("发送图像识别请求到 OpenAI, 模型: {}, 文件: {}, 大小: {} bytes", 
            model, imageFile.getOriginalFilename(), imageFile.getSize());
        
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            
            if (!response.isSuccessful()) {
                log.error("OpenAI API 调用失败: {}, 响应: {}", response.code(), responseBody);
                throw new IOException("OpenAI API 调用失败: " + response.code() + " - " + responseBody);
            }
            
            // 解析响应
            return parseOpenAIResponse(responseBody);
        }
    }
    
    /**
     * 构建包含图像的JSON请求体
     */
    private String buildVisionRequestJson(MultipartFile imageFile, String userMessage, String model) throws IOException {
        String imageDataUri = imageProcessingService.buildImageDataUri(imageFile);
        
        // 构建符合OpenAI Vision API格式的JSON请求
        String requestJson = String.format("""
            {
              "model": "%s",
              "messages": [
                {
                  "role": "user",
                  "content": [
                    {
                      "type": "text",
                      "text": "%s"
                    },
                    {
                      "type": "image_url",
                      "image_url": {
                        "url": "%s"
                      }
                    }
                  ]
                }
              ],
              "max_tokens": 1000
            }
            """, 
            model,
            userMessage != null ? userMessage.replace("\"", "\\\"") : "请描述这张图片的内容。",
            imageDataUri
        );
        
        return requestJson;
    }
    
    /**
     * 解析 OpenAI API 响应
     */
    private String parseOpenAIResponse(String responseBody) throws IOException {
        try {
            JsonNode responseJson = objectMapper.readTree(responseBody);
            JsonNode choices = responseJson.get("choices");
            
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode firstChoice = choices.get(0);
                JsonNode message = firstChoice.get("message");
                if (message != null) {
                    JsonNode content = message.get("content");
                    if (content != null) {
                        return content.asText();
                    }
                }
            }
            
            log.warn("OpenAI 响应格式异常: {}", responseBody);
            return "图像识别完成，但响应格式异常。";
            
        } catch (Exception e) {
            log.error("解析 OpenAI 响应失败", e);
            throw new IOException("解析 OpenAI 响应失败: " + e.getMessage());
        }
    }
} 
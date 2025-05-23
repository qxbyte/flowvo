package org.xue.app.client;

import lombok.AllArgsConstructor;
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
 * 根据模型自动选择对应的配置（OpenAI/DeepSeek）
 */
@Slf4j
@Component
public class OpenAiClient {

    // OpenAI配置
    private final String openaiApiKey;
    private final String openaiBaseUrl;
    private final boolean openaiProxyEnabled;
    private final String openaiProxyHost;
    private final int openaiProxyPort;
    private final double openaiTemperature;
    private final int openaiMaxTokens;
    
    // DeepSeek配置
    private final String deepseekApiKey;
    private final String deepseekBaseUrl;
    private final boolean deepseekProxyEnabled;
    private final String deepseekProxyHost;
    private final int deepseekProxyPort;
    private final double deepseekTemperature;
    private final int deepseekMaxTokens;
    
    // 通用配置
    private final int connectTimeout;
    private final int readTimeout;
    
    // 为不同服务创建专用的HttpClient
    private final OkHttpClient openaiHttpClient;
    private final OkHttpClient deepseekHttpClient;

    public OpenAiClient(
            // OpenAI配置
            @Value("${ai.openai.api-key}") String openaiApiKey, 
            @Value("${ai.openai.base-url}") String openaiBaseUrl,
            @Value("${ai.openai.proxy.enabled:false}") boolean openaiProxyEnabled,
            @Value("${ai.openai.proxy.host:}") String openaiProxyHost,
            @Value("${ai.openai.proxy.port:0}") int openaiProxyPort,
            @Value("${ai.openai.temperature:#{${ai.temperature:0.7}}}") double openaiTemperature,
            @Value("${ai.openai.max-tokens:#{${ai.max-tokens:2048}}}") int openaiMaxTokens,
            
            // DeepSeek配置
            @Value("${ai.deepseek.api-key}") String deepseekApiKey, 
            @Value("${ai.deepseek.base-url}") String deepseekBaseUrl,
            @Value("${ai.deepseek.proxy.enabled:false}") boolean deepseekProxyEnabled,
            @Value("${ai.deepseek.proxy.host:}") String deepseekProxyHost,
            @Value("${ai.deepseek.proxy.port:0}") int deepseekProxyPort,
            @Value("${ai.deepseek.temperature:#{${ai.temperature:0.8}}}") double deepseekTemperature,
            @Value("${ai.deepseek.max-tokens:#{${ai.max-tokens:2048}}}") int deepseekMaxTokens,
            
            // 通用配置 - 从ai根路径读取
            @Value("${ai.connect-timeout:30}") int connectTimeout,
            @Value("${ai.read-timeout:120}") int readTimeout
    ) {
        this.openaiApiKey = openaiApiKey;
        this.openaiBaseUrl = openaiBaseUrl;
        this.openaiProxyEnabled = openaiProxyEnabled;
        this.openaiProxyHost = openaiProxyHost;
        this.openaiProxyPort = openaiProxyPort;
        this.openaiTemperature = openaiTemperature;
        this.openaiMaxTokens = openaiMaxTokens;
        
        this.deepseekApiKey = deepseekApiKey;
        this.deepseekBaseUrl = deepseekBaseUrl;
        this.deepseekProxyEnabled = deepseekProxyEnabled;
        this.deepseekProxyHost = deepseekProxyHost;
        this.deepseekProxyPort = deepseekProxyPort;
        this.deepseekTemperature = deepseekTemperature;
        this.deepseekMaxTokens = deepseekMaxTokens;
        
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        
        // 创建OpenAI专用HttpClient
        this.openaiHttpClient = createHttpClient(openaiProxyEnabled, openaiProxyHost, openaiProxyPort, "OpenAI");
        
        // 创建DeepSeek专用HttpClient
        this.deepseekHttpClient = createHttpClient(deepseekProxyEnabled, deepseekProxyHost, deepseekProxyPort, "DeepSeek");
        
        log.info("OpenAI客户端初始化完成，OpenAI baseUrl={}, temperature={}, maxTokens={}, DeepSeek baseUrl={}, temperature={}, maxTokens={}", 
                openaiBaseUrl, openaiTemperature, openaiMaxTokens, deepseekBaseUrl, deepseekTemperature, deepseekMaxTokens);
    }

    /**
     * 创建HttpClient，支持代理配置
     */
    private OkHttpClient createHttpClient(boolean proxyEnabled, String proxyHost, int proxyPort, String serviceName) {
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
        
        // 如果启用代理，添加代理配置
        if (proxyEnabled && proxyHost != null && !proxyHost.isEmpty() && proxyPort > 0) {
            log.info("为{}服务配置代理: {}:{}", serviceName, proxyHost, proxyPort);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            builder.proxy(proxy);
        } else {
            log.info("{}服务未启用代理", serviceName);
        }
        
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
                        log.warn("{}服务端错误，尝试重试 ({}/{}): code={}, body={}", 
                                serviceName, retry + 1, maxRetries, response.code(), responseBody);
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
                    log.warn("{}请求发生IO异常，尝试重试 ({}/{}): {}", serviceName, retry + 1, maxRetries, e.getMessage());
                    
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
        
        return builder.build();
    }

    /**
     * 根据模型名称获取对应的配置信息
     */
    private ModelConfig getModelConfig(String model) {
        if (model == null) {
            // 默认使用DeepSeek配置（根据配置文件，主要使用DeepSeek）
            return new ModelConfig(deepseekApiKey, deepseekBaseUrl, deepseekHttpClient, deepseekTemperature, deepseekMaxTokens);
        }
        
        if (model.startsWith("gpt-") || model.contains("openai")) {
            log.info("使用OpenAI配置，模型: {}, temperature: {}, maxTokens: {}", model, openaiTemperature, openaiMaxTokens);
            return new ModelConfig(openaiApiKey, openaiBaseUrl, openaiHttpClient, openaiTemperature, openaiMaxTokens);
        } else if (model.startsWith("deepseek-") || model.contains("deepseek")) {
            log.info("使用DeepSeek配置，模型: {}, temperature: {}, maxTokens: {}", model, deepseekTemperature, deepseekMaxTokens);
            return new ModelConfig(deepseekApiKey, deepseekBaseUrl, deepseekHttpClient, deepseekTemperature, deepseekMaxTokens);
        } else {
            // 默认使用DeepSeek配置
            log.info("未识别的模型 {}，使用DeepSeek配置", model);
            return new ModelConfig(deepseekApiKey, deepseekBaseUrl, deepseekHttpClient, deepseekTemperature, deepseekMaxTokens);
        }
    }

    /**
     * 根据请求JSON中的模型信息动态选择配置并发送请求
     */
    public String chatSync(String requestJson) throws Exception {
        // 从请求JSON中提取模型信息
        String model = extractModelFromRequest(requestJson);
        return chatSync(requestJson, model);
    }

    /**
     * 根据传入的模型信息动态选择配置并发送请求
     * @param requestJson 请求JSON
     * @param model 模型名称
     * @return 响应JSON
     */
    public String chatSync(String requestJson, String model) throws Exception {
        ModelConfig config = getModelConfig(model);
        
        Request request = new Request.Builder()
                .url(config.baseUrl + "/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + config.apiKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Connection", "keep-alive")
                .post(RequestBody.create(requestJson, MediaType.parse("application/json")))
                .build();

        log.info("发送同步请求，模型: {}, baseUrl: {}", model, config.baseUrl);
        
        int maxRetries = 3;
        int retryCount = 0;
        Exception lastException = null;
        
        while (retryCount < maxRetries) {
            try {
                try (Response response = config.httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "无响应体";
                        log.error("API调用失败: code={}, body={}", response.code(), errorBody);
                        
                        if (response.code() >= 500 || response.code() == 429) {
                            // 服务器错误或请求过多，进行重试
                            retryCount++;
                            if (retryCount < maxRetries) {
                                log.info("服务器错误，{}ms后重试 ({}/{})", retryCount * 1000, retryCount, maxRetries);
                                Thread.sleep(retryCount * 1000); // 指数延迟
                                continue;
                            }
                        }
                        
                        throw new RuntimeException("调用API失败: " + response.code() + ", " + errorBody);
                    }
                    
                    String responseBody = response.body() != null ? response.body().string() : "";
                    log.info("收到API响应，模型: {}, 响应长度: {}", model, responseBody.length());
                    return responseBody;
                }
            } catch (IOException e) {
                lastException = e;
                log.error("API请求发送失败 (尝试 {}/{}): {}", retryCount + 1, maxRetries, e.getMessage(), e);
                
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
                log.error("处理API响应时出错: {}", e.getMessage(), e);
                
                retryCount++;
                if (retryCount < maxRetries) {
                    log.info("{}ms后重试 ({}/{})", retryCount * 1500, retryCount, maxRetries);
                    Thread.sleep(retryCount * 1500); // 1.5秒, 3秒, 4.5秒...
                    continue;
                }
                
                throw e;
            }
        }
        
        // 如果到这里，说明所有重试都失败了
        if (lastException instanceof RuntimeException) {
            throw (RuntimeException) lastException;
        } else if (lastException != null) {
            throw new RuntimeException("API调用最终失败", lastException);
        } else {
            throw new RuntimeException("API调用失败：未知错误");
        }
    }

    /**
     * 从请求JSON中提取模型名称
     */
    private String extractModelFromRequest(String requestJson) {
        try {
            // 简单的JSON解析，提取model字段
            if (requestJson.contains("\"model\"")) {
                int modelStart = requestJson.indexOf("\"model\"");
                int colonIndex = requestJson.indexOf(":", modelStart);
                if (colonIndex != -1) {
                    int valueStart = requestJson.indexOf("\"", colonIndex) + 1;
                    int valueEnd = requestJson.indexOf("\"", valueStart);
                    if (valueStart > 0 && valueEnd > valueStart) {
                        return requestJson.substring(valueStart, valueEnd);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析请求JSON中的模型信息失败: {}", e.getMessage());
        }
        return null; // 默认返回null，使用默认配置
    }

    /**
     * 获取指定模型的配置信息
     * @param model 模型名称
     * @return 模型配置信息
     */
    public ModelConfig getModelConfigForExternal(String model) {
        return getModelConfig(model);
    }

    /**
     * 获取指定模型的温度配置
     * @param model 模型名称
     * @return 温度值
     */
    public double getTemperatureForModel(String model) {
        return getModelConfig(model).temperature;
    }

    /**
     * 获取指定模型的最大token配置
     * @param model 模型名称
     * @return 最大token数
     */
    public int getMaxTokensForModel(String model) {
        return getModelConfig(model).maxTokens;
    }

    /**
     * 模型配置类
     */
    public static class ModelConfig {
        public final String apiKey;
        public final String baseUrl;
        public final OkHttpClient httpClient;
        public final double temperature;
        public final int maxTokens;
        
        public ModelConfig(String apiKey, String baseUrl, OkHttpClient httpClient, double temperature, int maxTokens) {
            this.apiKey = apiKey;
            this.baseUrl = baseUrl;
            this.httpClient = httpClient;
            this.temperature = temperature;
            this.maxTokens = maxTokens;
        }
    }
}
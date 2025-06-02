package org.xue.agents.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xue.agents.config.EmbeddingConfig;
import org.xue.agents.embed.EmbeddingClient;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 用于验证服务状态和依赖组件
 */
@Slf4j
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final VectorStore vectorStore;
    private final EmbeddingConfig embeddingConfig;
    private final EmbeddingClient embeddingClient; // 可选依赖

    @Autowired
    public HealthController(VectorStore vectorStore, 
                           EmbeddingConfig embeddingConfig,
                           @Autowired(required = false) EmbeddingClient embeddingClient) {
        this.vectorStore = vectorStore;
        this.embeddingConfig = embeddingConfig;
        this.embeddingClient = embeddingClient;
    }

    /**
     * 健康检查接口
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 检查VectorStore
            String vectorStoreName = vectorStore.getName();
            response.put("vectorStore", "OK - " + vectorStoreName);
            
            // 显示向量化配置信息
            response.put("embeddingType", embeddingConfig.getType().toString());
            
            // 检查EmbeddingClient（仅在EXTERNAL模式下）
            if (embeddingConfig.getType() == EmbeddingConfig.ServiceType.EXTERNAL) {
                if (embeddingClient != null) {
                    try {
                        response.put("embeddingClient", "OK - 外部向量化服务客户端已注入");
                        response.put("externalServiceUrl", embeddingConfig.getExternal().getUrl());
                    } catch (Exception e) {
                        response.put("embeddingClient", "WARNING - " + e.getMessage());
                    }
                } else {
                    response.put("embeddingClient", "ERROR - 外部向量化服务客户端未启用");
                }
            } else {
                response.put("embeddingClient", "N/A - 使用Spring AI内置向量化服务");
            }
            
            // 显示配置信息
            Map<String, Object> config = new HashMap<>();
            config.put("chunkSize", embeddingConfig.getExternal().getChunkSize());
            config.put("chunkOverlap", embeddingConfig.getExternal().getChunkOverlap());
            response.put("textSplitterConfig", config);
            
            response.put("status", "UP");
            response.put("service", "agents");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("健康检查失败", e);
            response.put("status", "DOWN");
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 
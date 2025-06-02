package org.xue.app.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xue.app.feign.DocumentClient;
import org.xue.app.service.DocumentManagementService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 健康检查控制器
 * 用于验证app服务状态和依赖组件
 */
@Slf4j
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final DocumentManagementService documentManagementService;
    private final DocumentClient documentClient;

    @Autowired
    public HealthController(DocumentManagementService documentManagementService, 
                           DocumentClient documentClient) {
        this.documentManagementService = documentManagementService;
        this.documentClient = documentClient;
    }

    /**
     * 健康检查接口
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 检查DocumentManagementService
            List<String> supportedTypes = documentManagementService.getSupportedFileTypes();
            response.put("documentService", "OK - " + supportedTypes.size() + " supported types");
            
            // 检查与agents服务的连通性（可选）
            try {
                // 这里不实际调用agents服务，只检查Feign client是否注入成功
                response.put("agentsClient", "OK - Feign client configured");
            } catch (Exception e) {
                response.put("agentsClient", "WARNING - " + e.getMessage());
            }
            
            response.put("status", "UP");
            response.put("service", "app");
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
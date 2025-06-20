package org.xue.app.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
// import org.xue.app.feign.DocumentClient; // 已移除
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

    @Autowired
    public HealthController(DocumentManagementService documentManagementService) {
        this.documentManagementService = documentManagementService;
    }

    /**
     * 健康检查接口
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 检查DocumentManagementService（本地服务）
            List<String> supportedTypes = documentManagementService.getSupportedFileTypes();
            response.put("documentService", "OK - " + supportedTypes.size() + " supported types");
            
            // 不再检查agents服务连通性，因为已经解耦
            response.put("agentsClient", "DECOUPLED - Agents service is now independent");
            
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
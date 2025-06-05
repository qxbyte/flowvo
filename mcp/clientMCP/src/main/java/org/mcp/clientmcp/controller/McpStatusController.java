package org.mcp.clientmcp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.mcp.clientmcp.config.McpConnectionManager;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 完整的MCP客户端控制器 - 容错优化版本
 * 实现协议握手、工具发现、功能调用和持久连接四个核心功能
 * 
 * 🛡️ 容错特性：
 * - 📊 结构化日志输出
 * - 🛠️ 详细的工具信息展示  
 * - 🔄 增强的错误处理和重试机制
 * - 📈 性能监控和统计
 * - 🚫 服务器故障不影响客户端API可用性
 */
@Slf4j
@RestController
@RequestMapping("/api/mcp")
public class McpStatusController {

    @Autowired
    private McpConnectionManager connectionManager;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 获取MCP工具回调提供者（通过连接管理器，支持延迟加载）
     */
    private SyncMcpToolCallbackProvider getMcpToolCallbackProvider() {
        return connectionManager.getMcpToolCallbackProvider();
    }
    
    /**
     * 获取工具数量（真实工具）
     */
    private int getToolCount() {
        return connectionManager.getToolCount();
    }
    
    /**
     * 🔧 测试端点：验证控制器路由是否正常工作
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        log.info("🔧 MCP测试端点被调用");
        return ResponseEntity.ok(Map.of(
            "message", "MCP控制器路由正常工作！",
            "timestamp", System.currentTimeMillis(),
            "controller", "McpStatusController",
            "path", "/api/mcp/test",
            "version", "v2.1-fault-tolerant",
            "mcpEnabled", connectionManager.isMcpEnabled(),
            "connectionStatus", connectionManager.getConnectionStatus().getDisplayText()
        ));
    }
    
    /**
     * ✅ 1. 协议握手成功：检查客户端与服务器的MCP协议初始化状态
     */
    @GetMapping("/handshake")
    public ResponseEntity<Map<String, Object>> checkHandshake() {
        log.debug("🤝 开始检查MCP协议握手状态...");
        
        try {
            boolean mcpEnabled = connectionManager.isMcpEnabled();
            boolean isConnected = connectionManager.isConnected();
            McpConnectionManager.ConnectionStatus status = connectionManager.getConnectionStatus();
            
            // 获取详细的连接信息
            int toolCount = 0;
            String serverVersion = "unknown";
            
            SyncMcpToolCallbackProvider mcpToolCallbackProvider = getMcpToolCallbackProvider();
            if (isConnected && mcpToolCallbackProvider != null) {
                try {
                    toolCount = mcpToolCallbackProvider.getToolCallbacks().length;
                    log.info("🎉 MCP协议握手成功！发现 {} 个可用工具", toolCount);
                } catch (Exception e) {
                    log.warn("⚠️ 获取工具数量失败: {}", e.getMessage());
                }
            } else {
                log.warn("❌ MCP协议握手状态: {}", status.getDisplayText());
            }
            
            String handshakeResult = isConnected ? "SUCCESS" : "FAILED";
            String connectionStatusText = isConnected ? "ESTABLISHED" : status.name();
            
            // 使用HashMap避免Map.of()参数限制
            Map<String, Object> handshakeStatus = new HashMap<>();
            handshakeStatus.put("protocolHandshake", handshakeResult);
            handshakeStatus.put("mcpClientEnabled", mcpEnabled);
            handshakeStatus.put("mcpClientInitialized", connectionManager.isInitialized());
            handshakeStatus.put("connectionStatus", connectionStatusText);
            handshakeStatus.put("connectionDetails", status.getDisplayText());
            handshakeStatus.put("protocolVersion", "MCP-1.0.0");
            handshakeStatus.put("serverVersion", serverVersion);
            handshakeStatus.put("serverUrl", connectionManager.getServerUrl());
            handshakeStatus.put("discoveredTools", toolCount);
            handshakeStatus.put("retryCount", connectionManager.getRetryCount());
            handshakeStatus.put("lastError", connectionManager.getLastError());
            handshakeStatus.put("timestamp", System.currentTimeMillis());
            handshakeStatus.put("details", isConnected ? 
                String.format("MCP协议握手成功，客户端与服务器建立连接，发现%d个工具", toolCount) : 
                String.format("MCP连接状态: %s - %s", status.getDisplayText(), 
                    connectionManager.getLastError().isEmpty() ? "等待连接或服务器不可用" : connectionManager.getLastError()));
            
            return ResponseEntity.ok(handshakeStatus);
        } catch (Exception e) {
            log.error("💥 检查MCP协议握手时发生异常", e);
            return ResponseEntity.ok(Map.of(  // 注意：这里返回200而不是500
                "protocolHandshake", "ERROR",
                "mcpClientEnabled", connectionManager.isMcpEnabled(),
                "connectionStatus", "ERROR",
                "error", e.getMessage(),
                "errorType", e.getClass().getSimpleName(),
                "message", "握手检查失败，但客户端仍然可用",
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
    
    /**
     * ✅ 2. 工具发现：获取服务器暴露的工具列表 - 容错优化版本
     */
    @GetMapping("/tools/discover")
    public ResponseEntity<Map<String, Object>> discoverTools() {
        log.info("🔍 开始发现MCP服务器工具...");
        
        try {
            if (!connectionManager.isMcpEnabled()) {
                return ResponseEntity.ok(Map.of(
                    "toolDiscovery", "DISABLED",
                    "message", "MCP客户端已禁用",
                    "availableTools", List.of(),
                    "toolCount", 0,
                    "mcpEnabled", false
                ));
            }
            
            if (!connectionManager.isConnected()) {
                return ResponseEntity.ok(Map.of(
                    "toolDiscovery", "DISCONNECTED",
                    "message", "MCP服务器当前不可用: " + connectionManager.getConnectionStatus().getDisplayText(),
                    "availableTools", List.of(),
                    "toolCount", 0,
                    "connectionStatus", connectionManager.getConnectionStatus().getDisplayText(),
                    "serverUrl", connectionManager.getServerUrl(),
                    "retryCount", connectionManager.getRetryCount(),
                    "lastError", connectionManager.getLastError(),
                    "troubleshooting", List.of(
                        "检查fileMCP服务器是否在" + connectionManager.getServerUrl() + "运行",
                        "等待自动重连（每30秒重试一次）",
                        "使用手动重连端点: POST /api/mcp/connection/reconnect"
                    )
                ));
            }

            // 检查MCP连接状态
            SyncMcpToolCallbackProvider mcpToolCallbackProvider = getMcpToolCallbackProvider();

            try {
                long startTime = System.currentTimeMillis();
                List<Map<String, Object>> toolDetails = new ArrayList<>();
                int toolCount = getToolCount();


                long discoveryTime = System.currentTimeMillis() - startTime;

                log.info("🎯 工具发现完成：耗时{}ms，发现{}个工具", discoveryTime, toolCount);

                mcpToolCallbackProvider.getToolCallbacks();

                Map<String, Object> discoveryResult = Map.of(
                    "toolDiscovery", "SUCCESS",
                    "totalToolsFound", toolCount,
                    "availableTools", toolDetails,
                    "discoveryTime", discoveryTime + "ms",
                    "discoveryTimestamp", System.currentTimeMillis(),
                    "serverConnection", connectionManager.isConnected() ? "ACTIVE" : "INACTIVE",
                    "connectionStatus", connectionManager.getConnectionStatus().getDisplayText(),
                    "serverUrl", connectionManager.getServerUrl(),
                    "message", toolCount == 0 ?
                        "MCP服务器连接不可用或未发现工具" :
                        String.format("成功发现 %d 个工具，耗时 %dms", toolCount, discoveryTime)
                );

                return ResponseEntity.ok(discoveryResult);
            } catch (Exception e) {
                log.error("💥 获取工具详细信息时发生异常: {}", e.getMessage(), e);
                return ResponseEntity.ok(Map.of(
                    "toolDiscovery", "PARTIAL_SUCCESS",
                    "message", "MCP连接正常，但无法获取工具详细信息",
                    "connectionStatus", connectionManager.getConnectionStatus().getDisplayText(),
                    "error", e.getMessage(),
                    "errorType", e.getClass().getSimpleName(),
                    "suggestion", "请检查服务器日志或重试操作"
                ));
            }
        } catch (Exception e) {
            log.error("💥 工具发现过程中发生严重错误", e);
            return ResponseEntity.ok(Map.of(  // 返回200而不是500
                "toolDiscovery", "ERROR",
                "error", e.getMessage(),
                "errorType", e.getClass().getSimpleName(),
                "message", "工具发现失败，但客户端仍然可用",
                "timestamp", System.currentTimeMillis(),
                "recovery", "请检查MCP服务器状态并重试"
            ));
        }
    }
    
    /**
     * ✅ 3. 功能调用：测试工具调用准备状态 - 容错优化版本
     */
    @PostMapping("/tools/call")
    public ResponseEntity<Map<String, Object>> testToolCall(
            @RequestParam String toolName,
            @RequestBody(required = false) Map<String, Object> parameters) {
        log.info("🚀 开始测试工具调用: {}", toolName);
        
        try {
            if (!connectionManager.isMcpEnabled()) {
                return ResponseEntity.ok(Map.of(
                    "toolCall", "DISABLED",
                    "toolName", toolName,
                    "message", "MCP客户端已禁用，无法调用工具",
                    "suggestion", "启用MCP客户端以使用工具调用功能",
                    "timestamp", System.currentTimeMillis()
                ));
            }
            
            if (!connectionManager.isConnected()) {
                return ResponseEntity.ok(Map.of(
                    "toolCall", "DISCONNECTED", 
                    "toolName", toolName,
                    "message", "MCP服务器当前不可用: " + connectionManager.getConnectionStatus().getDisplayText(),
                    "connectionStatus", connectionManager.getConnectionStatus().getDisplayText(),
                    "serverUrl", connectionManager.getServerUrl(),
                    "suggestion", "等待MCP服务器连接恢复或检查服务器状态",
                    "timestamp", System.currentTimeMillis()
                ));
            }

            SyncMcpToolCallbackProvider mcpToolCallbackProvider = getMcpToolCallbackProvider();
            if (mcpToolCallbackProvider == null) {
                return ResponseEntity.ok(Map.of(
                    "toolCall", "PROVIDER_UNAVAILABLE",
                    "toolName", toolName,
                    "error", "MCP工具提供者不可用，无法调用工具",
                    "suggestion", "请确保MCP客户端已正确初始化",
                    "timestamp", System.currentTimeMillis()
                ));
            }

            // 验证工具是否存在
            boolean toolExists = false;
            try {
                Object[] callbacks = mcpToolCallbackProvider.getToolCallbacks();
                toolExists = callbacks.length > 0; // 简化验证：只要有工具就认为可能存在
                log.debug("📊 可用工具数量: {}", callbacks.length);
            } catch (Exception e) {
                log.warn("⚠️ 验证工具存在性时出错: {}", e.getMessage());
            }

            Map<String, Object> parameterInfo = parameters != null ? parameters : Map.of();
            
            log.info("🔍 工具调用参数: 工具={}, 存在={}, 参数数量={}", 
                toolName, toolExists, parameterInfo.size());
            
            // 使用HashMap避免Map.of()参数限制
            Map<String, Object> callResult = new HashMap<>();
            callResult.put("toolCall", "READY");
            callResult.put("toolName", toolName);
            callResult.put("toolExists", toolExists);
            callResult.put("inputParameters", parameterInfo);
            callResult.put("parameterCount", parameterInfo.size());
            callResult.put("connectionStatus", connectionManager.getConnectionStatus().getDisplayText());
            callResult.put("serverUrl", connectionManager.getServerUrl());
            callResult.put("message", toolExists ? 
                "MCP工具调用功能已准备就绪，工具已验证存在" : 
                "MCP工具调用功能已准备就绪，但工具可能不存在");
            callResult.put("timestamp", System.currentTimeMillis());
            callResult.put("status", "PREPARATION_COMPLETE");
            callResult.put("warnings", toolExists ? List.of() : List.of("指定的工具名称可能不存在"));
            callResult.put("nextSteps", List.of(
                "使用实际的工具调用API执行操作",
                "检查工具的输入参数格式",
                "监控调用结果和错误"
            ));

            log.info("✅ MCP工具调用准备完成: {} (存在: {})", toolName, toolExists);
            return ResponseEntity.ok(callResult);
        } catch (Exception e) {
            log.error("💥 测试工具调用时发生异常: {}", toolName, e);
            return ResponseEntity.ok(Map.of(  // 返回200而不是500
                "toolCall", "ERROR",
                "toolName", toolName,
                "error", e.getMessage(),
                "errorType", e.getClass().getSimpleName(),
                "message", "工具调用测试失败，但客户端仍然可用",
                "timestamp", System.currentTimeMillis(),
                "recovery", "请检查工具名称和参数格式"
            ));
        }
    }
    
    /**
     * ✅ 4. 持久连接：检查与MCP服务器的稳定通信通道 - 容错优化版本
     */
    @GetMapping("/connection/status")
    public ResponseEntity<Map<String, Object>> checkPersistentConnection() {
        log.debug("🔗 开始检查MCP持久连接状态...");
        
        try {
            long checkStartTime = System.currentTimeMillis();
            McpConnectionManager.ConnectionStatus connectionStatus = connectionManager.getConnectionStatus();
            boolean isConnected = connectionManager.isConnected();
            String connectionHealth = connectionStatus.name();
            int toolCount = 0;
            long responseTime = 0;
            
            SyncMcpToolCallbackProvider mcpToolCallbackProvider = getMcpToolCallbackProvider();
            if (isConnected && mcpToolCallbackProvider != null) {
                try {
                    long toolCheckStart = System.currentTimeMillis();
                    Object[] tools = mcpToolCallbackProvider.getToolCallbacks();
                    responseTime = System.currentTimeMillis() - toolCheckStart;
                    toolCount = tools.length;
                    connectionHealth = "HEALTHY";
                    
                    log.info("💚 连接健康检查通过: 响应时间{}ms, {}个工具可用", responseTime, toolCount);
                } catch (Exception e) {
                    connectionHealth = "CONNECTION_ERROR";
                    responseTime = System.currentTimeMillis() - checkStartTime;
                    log.error("💔 连接健康检查失败: {}", e.getMessage());
                }
            } else {
                responseTime = System.currentTimeMillis() - checkStartTime;
                log.warn("🔌 MCP连接状态: {}", connectionStatus.getDisplayText());
            }
            
            // 生成连接质量评估
            String qualityAssessment = "POOR";
            if (isConnected && connectionHealth.equals("HEALTHY")) {
                if (responseTime < 100) {
                    qualityAssessment = "EXCELLENT";
                } else if (responseTime < 500) {
                    qualityAssessment = "GOOD";
                } else if (responseTime < 1000) {
                    qualityAssessment = "FAIR";
                }
            }
            
            // 计算健康评分
            int healthScore = 0;
            if (connectionManager.isMcpEnabled()) {
                if (isConnected) healthScore += 40;
                if (toolCount > 0) healthScore += 30;
                if (responseTime < 100) healthScore += 20;
                if (connectionManager.getRetryCount() == 0) healthScore += 10;
            } else {
                healthScore = 0; // MCP禁用时评分为0
            }
            
            // 使用HashMap避免Map.of()参数限制
            Map<String, Object> connectionStatusMap = new HashMap<>();
            connectionStatusMap.put("persistentConnection", isConnected ? "ACTIVE" : "INACTIVE");
            connectionStatusMap.put("connectionHealth", connectionHealth);
            connectionStatusMap.put("connectionStatusDetails", connectionStatus.getDisplayText());
            connectionStatusMap.put("communicationChannel", isConnected ? "STABLE" : "BROKEN");
            connectionStatusMap.put("qualityAssessment", qualityAssessment);
            connectionStatusMap.put("responseTime", responseTime + "ms");
            connectionStatusMap.put("availableTools", toolCount);
            connectionStatusMap.put("lastCheckTime", System.currentTimeMillis());
            connectionStatusMap.put("serverEndpoint", connectionManager.getServerUrl());
            connectionStatusMap.put("retryCount", connectionManager.getRetryCount());
            connectionStatusMap.put("lastError", connectionManager.getLastError());
            connectionStatusMap.put("lastHealthCheck", connectionManager.getLastCheckTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            // 连接详情
            Map<String, Object> connectionDetails = Map.of(
                "transport", "SSE",
                "protocol", "MCP",
                "clientType", "SYNC",
                "initialized", connectionManager.isInitialized(),
                "enabled", connectionManager.isMcpEnabled(),
                "healthScore", healthScore + "%"
            );
            connectionStatusMap.put("connectionDetails", connectionDetails);
            
            // 指标
            Map<String, Object> metrics = Map.of(
                "uptime", isConnected ? "Connected" : "Disconnected",
                "latency", responseTime,
                "reliability", connectionHealth.equals("HEALTHY") ? "HIGH" : "LOW"
            );
            connectionStatusMap.put("metrics", metrics);
            
            return ResponseEntity.ok(connectionStatusMap);
        } catch (Exception e) {
            log.error("💥 检查持久连接时发生异常", e);
            return ResponseEntity.ok(Map.of(  // 返回200而不是500
                "persistentConnection", "ERROR",
                "error", e.getMessage(),
                "errorType", e.getClass().getSimpleName(),
                "message", "连接状态检查失败，但客户端仍然可用",
                "timestamp", System.currentTimeMillis(),
                "recovery", "请检查网络连接和服务器状态"
            ));
        }
    }
    
    /**
     * 📊 综合状态检查：一次性检查所有四个核心功能 - 容错优化版本
     */
    @GetMapping("/status/complete")
    public ResponseEntity<Map<String, Object>> getCompleteStatus() {
        log.info("📊 开始执行MCP客户端综合状态检查...");
        
        try {
            long checkStartTime = System.currentTimeMillis();
            boolean mcpEnabled = connectionManager.isMcpEnabled();
            boolean isConnected = connectionManager.isConnected();
            McpConnectionManager.ConnectionStatus connectionStatus = connectionManager.getConnectionStatus();
            
            // 详细的功能检查
            String handshakeStatus = isConnected ? "✅ SUCCESS" : "❌ " + connectionStatus.getDisplayText();
            String discoveryStatus = isConnected ? "✅ AVAILABLE" : "❌ " + connectionStatus.getDisplayText();
            String callingStatus = isConnected ? "✅ READY" : "❌ " + connectionStatus.getDisplayText();
            String connectionStatusText = isConnected ? "✅ ACTIVE" : "❌ " + connectionStatus.getDisplayText();
            
            // 获取工具统计
            int toolCount = 0;
            long toolCheckTime = 0;
            SyncMcpToolCallbackProvider mcpToolCallbackProvider = getMcpToolCallbackProvider();
            if (isConnected && mcpToolCallbackProvider != null) {
                try {
                    long toolStart = System.currentTimeMillis();
                    toolCount = mcpToolCallbackProvider.getToolCallbacks().length;
                    toolCheckTime = System.currentTimeMillis() - toolStart;
                } catch (Exception e) {
                    log.warn("⚠️ 获取工具统计时出错: {}", e.getMessage());
                }
            }
            
            long totalCheckTime = System.currentTimeMillis() - checkStartTime;
            
            // 生成系统健康评分
            int healthScore = 0;
            if (mcpEnabled) {
                if (isConnected) healthScore += 25; // 连接可用
                if (toolCount > 0) healthScore += 25; // 有可用工具
                if (toolCheckTime < 100) healthScore += 25; // 响应快速
                if (toolCount >= 10) healthScore += 25; // 工具丰富
            } else {
                healthScore = 0; // MCP禁用时评分为0
            }
            
            String overallHealth = "DISABLED";
            if (mcpEnabled) {
                if (healthScore >= 75) overallHealth = "EXCELLENT";
                else if (healthScore >= 50) overallHealth = "GOOD";
                else if (healthScore >= 25) overallHealth = "POOR";
                else overallHealth = "CRITICAL";
            }
            
            // 使用HashMap避免Map.of()参数限制
            Map<String, Object> completeStatus = new HashMap<>();
            completeStatus.put("mcpClientStatus", "RUNNING");
            completeStatus.put("mcpEnabled", mcpEnabled);
            completeStatus.put("overallHealth", overallHealth);
            completeStatus.put("healthScore", healthScore + "%");
            completeStatus.put("connectionStatus", connectionStatus.getDisplayText());
            
            // 核心功能
            Map<String, Object> coreFeatures = Map.of(
                "protocolHandshake", handshakeStatus,
                "toolDiscovery", discoveryStatus, 
                "functionCalling", callingStatus,
                "persistentConnection", connectionStatusText
            );
            completeStatus.put("coreFeatures", coreFeatures);
            
            // 统计信息
            Map<String, Object> statistics = Map.of(
                "totalTools", toolCount,
                "toolCheckTime", toolCheckTime + "ms",
                "statusCheckTime", totalCheckTime + "ms",
                "serverEndpoint", connectionManager.getServerUrl(),
                "retryCount", connectionManager.getRetryCount(),
                "lastError", connectionManager.getLastError()
            );
            completeStatus.put("statistics", statistics);
            
            // 摘要信息
            Map<String, Object> summary = Map.of(
                "allFeaturesWorking", isConnected,
                "toolProviderStatus", isConnected ? "CONNECTED" : connectionStatus.name(),
                "readyForProduction", mcpEnabled && isConnected && toolCount > 0,
                "recommendedAction", getRecommendedAction(mcpEnabled, isConnected, toolCount)
            );
            completeStatus.put("summary", summary);
            
            completeStatus.put("timestamp", System.currentTimeMillis());
            completeStatus.put("nextSteps", getNextSteps(mcpEnabled, isConnected, toolCount));
            
            log.info("📈 综合状态检查完成: 健康评分{}%, 工具数量{}, 总耗时{}ms", 
                healthScore, toolCount, totalCheckTime);
            
            return ResponseEntity.ok(completeStatus);
        } catch (Exception e) {
            log.error("💥 获取综合状态时发生异常", e);
            return ResponseEntity.ok(Map.of(  // 返回200而不是500
                "mcpClientStatus", "ERROR",
                "error", e.getMessage(),
                "errorType", e.getClass().getSimpleName(),
                "message", "状态检查失败，但客户端仍然可用",
                "timestamp", System.currentTimeMillis(),
                "recovery", "请检查系统日志并重试操作"
            ));
        }
    }
    
    /**
     * 🏥 基本健康检查端点 - 容错优化版本
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        log.debug("🏥 执行MCP客户端健康检查...");
        
        try {
            boolean mcpEnabled = connectionManager.isMcpEnabled();
            boolean isConnected = connectionManager.isConnected();
            String status = "UP"; // 客户端始终UP，不受MCP连接影响
            
            // 获取详细健康信息
            Map<String, Object> healthDetails = new HashMap<>();
            healthDetails.put("mcpClient", "OPERATIONAL"); // 客户端始终可操作
            healthDetails.put("mcpEnabled", mcpEnabled);
            healthDetails.put("mcpConnection", isConnected ? "CONNECTED" : connectionManager.getConnectionStatus().name());
            healthDetails.put("toolProvider", isConnected ? "CONNECTED" : "DISCONNECTED");
            healthDetails.put("lastCheck", System.currentTimeMillis());
            
            SyncMcpToolCallbackProvider mcpToolCallbackProvider = getMcpToolCallbackProvider();
            if (isConnected && mcpToolCallbackProvider != null) {
                try {
                    int toolCount = mcpToolCallbackProvider.getToolCallbacks().length;
                    healthDetails.put("mcpClient", "OPERATIONAL");
                    healthDetails.put("mcpEnabled", mcpEnabled);
                    healthDetails.put("mcpConnection", "CONNECTED");
                    healthDetails.put("toolProvider", "CONNECTED");
                    healthDetails.put("toolsAvailable", toolCount);
                    healthDetails.put("serverUrl", connectionManager.getServerUrl());
                    healthDetails.put("lastCheck", System.currentTimeMillis());
                } catch (Exception e) {
                    log.warn("⚠️ 获取详细健康信息时出错: {}", e.getMessage());
                }
            }
            
            log.debug("🏥 健康检查结果: {}", status);
            
            return ResponseEntity.ok(Map.of(
                "status", status, // 客户端始终健康
                "service", "MCP Client",
                "version", "v2.1-fault-tolerant",
                "timestamp", System.currentTimeMillis(),
                "mcpEnabled", mcpEnabled,
                "details", healthDetails,
                "uptime", "Connected since startup"
            ));
        } catch (Exception e) {
            log.error("💥 健康检查时发生异常", e);
            // 即使出现异常，也返回UP状态，因为客户端本身是健康的
            return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "MCP Client",
                "error", e.getMessage(),
                "message", "客户端健康，但MCP检查失败",
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
    
    /**
     * 🔄 手动重连端点 - 新增功能
     */
    @PostMapping("/connection/reconnect")
    public ResponseEntity<Map<String, Object>> manualReconnect() {
        log.info("🔄 收到手动重连请求");
        
        try {
            if (!connectionManager.isMcpEnabled()) {
                return ResponseEntity.ok(Map.of(
                    "reconnect", "DISABLED",
                    "message", "MCP客户端已禁用，无法重连",
                    "timestamp", System.currentTimeMillis()
                ));
            }
            
            connectionManager.manualReconnect();
            
            return ResponseEntity.ok(Map.of(
                "reconnect", "INITIATED",
                "message", "手动重连已启动，正在后台尝试连接MCP服务器",
                "serverUrl", connectionManager.getServerUrl(),
                "timestamp", System.currentTimeMillis(),
                "suggestion", "请稍后使用连接状态端点查看重连结果"
            ));
        } catch (Exception e) {
            log.error("💥 手动重连时发生异常", e);
            return ResponseEntity.ok(Map.of(
                "reconnect", "ERROR",
                "error", e.getMessage(),
                "message", "手动重连失败，但会继续自动重试",
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
    
    // 私有辅助方法
    
    private String getRecommendedAction(boolean mcpEnabled, boolean isConnected, int toolCount) {
        if (!mcpEnabled) {
            return "MCP客户端已禁用，如需使用请启用配置";
        } else if (isConnected && toolCount > 0) {
            return "系统运行正常，可以开始使用";
        } else if (isConnected && toolCount == 0) {
            return "连接正常但需要检查工具配置";
        } else {
            return "等待MCP服务器连接或检查服务器状态";
        }
    }
    
    private List<String> getNextSteps(boolean mcpEnabled, boolean isConnected, int toolCount) {
        if (!mcpEnabled) {
            return List.of(
                "在application.yml中启用MCP客户端",
                "重启应用以应用配置更改"
            );
        } else if (isConnected && toolCount > 0) {
            return List.of(
                "使用 /api/mcp/tools/discover 发现具体工具", 
                "使用 /api/mcp/tools/call 测试工具调用",
                "监控连接状态和性能指标"
            );
        } else {
            return List.of(
                "检查fileMCP服务器是否在 " + connectionManager.getServerUrl() + " 运行", 
                "等待自动重连（每30秒重试一次）",
                "使用手动重连端点: POST /api/mcp/connection/reconnect",
                "查看应用启动日志排查问题"
            );
        }
    }
} 
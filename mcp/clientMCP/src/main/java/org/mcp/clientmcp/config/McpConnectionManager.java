package org.mcp.clientmcp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * MCP连接管理器 - Spring AI标准配置版本
 * 
 * 🛡️ 核心特性：
 * - Spring AI自动装配：使用Spring AI的MCP客户端自动配置
 * - 真实工具获取：直接获取server端的真实tools
 * - 故障隔离：MCP服务器故障不影响客户端其他功能
 * - 状态监控：实时跟踪连接状态和健康情况
 */
@Slf4j
@Component
@EnableAsync
public class McpConnectionManager {

    public enum ConnectionStatus {
        DISCONNECTED("🔴", "未连接"),
        CONNECTING("🟡", "连接中"),
        CONNECTED("🟢", "已连接"),
        FAILED("❌", "连接失败"),
        RECOVERING("🔄", "恢复中");
        
        private final String emoji;
        private final String description;
        
        ConnectionStatus(String emoji, String description) {
            this.emoji = emoji;
            this.description = description;
        }
        
        public String getDisplayText() {
            return emoji + " " + description;
        }
    }

    // 直接注入Spring AI的MCP工具回调提供者
    @Autowired(required = false)
    private SyncMcpToolCallbackProvider mcpToolCallbackProvider;
    
    @Value("${spring.ai.mcp.client.sse.connections.file-server.url:http://localhost:19091}")
    private String serverUrl;
    
    @Value("${spring.ai.mcp.client.enabled:true}")
    private boolean mcpEnabled;
    
    @Value("${spring.ai.mcp.client.options.connect-timeout:10s}")
    private String connectionTimeoutStr;
    
    @Value("${spring.ai.mcp.client.sse.connections.file-server.retry.max-attempts:10}")
    private int maxRetryAttempts;
    
    private int connectionTimeoutMs = 10000;  // 默认10秒
    private long retryIntervalMs = 10000;     // 默认10秒 - 减少重试延迟
    private long healthCheckIntervalMs = 30000; // 默认30秒
    
    // 连接状态管理
    private final AtomicReference<ConnectionStatus> connectionStatus = new AtomicReference<>(ConnectionStatus.DISCONNECTED);
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);
    private final AtomicInteger retryCount = new AtomicInteger(0);
    private final AtomicReference<String> lastError = new AtomicReference<>("");
    private final AtomicReference<LocalDateTime> lastCheckTime = new AtomicReference<>(LocalDateTime.now());

    /**
     * 应用启动完成后检查MCP连接状态
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (!mcpEnabled) {
            log.info("🚫 MCP客户端已禁用，跳过连接检查");
            connectionStatus.set(ConnectionStatus.DISCONNECTED);
            return;
        }
        
        log.info("🚀 应用启动完成，开始检查MCP连接...");
        checkMcpConnectionAsync();
    }
    
    /**
     * 异步检查MCP连接状态
     */
    @Async
    public void checkMcpConnectionAsync() {
        try {
            log.info("🔗 检查Spring AI MCP连接状态...");
            connectionStatus.set(ConnectionStatus.CONNECTING);
            lastCheckTime.set(LocalDateTime.now());
            
            if (mcpToolCallbackProvider != null) {
                // 测试工具回调是否可用
                Object[] tools = mcpToolCallbackProvider.getToolCallbacks();
                int toolCount = tools.length;
                
                connectionStatus.set(ConnectionStatus.CONNECTED);
                retryCount.set(0);
                lastError.set("");
                isInitialized.set(true);


                // 这里需要改一下 XXX MCP连接成功
                log.info("✅ MCP连接成功!");
                log.info("   🎯 服务器地址: {}", serverUrl);
                log.info("   🛠️ 可用工具数量: {}", toolCount);
                log.info("   📡 连接状态: {}", connectionStatus.get().getDisplayText());
                log.info("   🕐 连接时间: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                
                if (toolCount > 0) {
                    log.info("   🎉 MCP服务器完全就绪，所有功能可用");
                } else {
                    log.warn("   ⚠️ MCP服务器已连接但未发现可用工具");
                }
                
            } else {
                handleConnectionFailure("Spring AI MCP工具回调提供者不可用 - 可能服务器未启动或配置错误");
            }
            
        } catch (Exception e) {
            handleConnectionFailure("MCP连接检查异常: " + e.getMessage());
        }
    }
    
    /**
     * 处理连接失败
     */
    private void handleConnectionFailure(String errorMessage) {
        connectionStatus.set(ConnectionStatus.FAILED);
        lastError.set(errorMessage);
        retryCount.incrementAndGet();
        
        log.warn("⚠️ MCP连接失败 (第{}次尝试)", retryCount.get());
        log.warn("   📍 失败原因: {}", errorMessage);
        log.warn("   🎯 服务器地址: {}", serverUrl);
        log.warn("   📊 连接状态: {}", connectionStatus.get().getDisplayText());
        
        if (retryCount.get() >= maxRetryAttempts) {
            log.warn("   🚫 已达到最大重试次数({}次)，停止自动重试", maxRetryAttempts);
            log.warn("   💡 建议检查fileMCP服务器状态或手动重启客户端");
        } else {
            log.info("   🔄 将在{}秒后进行下次重试...", retryIntervalMs / 1000);
        }
    }
    
    /**
     * 检查服务器是否可达
     */
    private boolean isServerReachable() {
        try {
            URL url = new URL(serverUrl + "/actuator/health");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(connectionTimeoutMs);
            connection.setReadTimeout(connectionTimeoutMs);
            
            int responseCode = connection.getResponseCode();
            boolean reachable = responseCode == 200;
            
            if (reachable) {
                log.debug("🌐 服务器可达性检查通过: {} -> {}", serverUrl, responseCode);
            } else {
                log.debug("🌐 服务器不可达: {} -> {}", serverUrl, responseCode);
            }
            
            return reachable;
        } catch (Exception e) {
            log.debug("🌐 服务器可达性检查失败: {} - {}", serverUrl, e.getMessage());
            return false;
        }
    }
    
    /**
     * 定期重试连接（仅在连接失败且未超过最大重试次数时）
     */
    @Scheduled(fixedRate = 10000)
    public void retryConnectionIfNeeded() {
        if (!mcpEnabled) {
            return;
        }
        
        ConnectionStatus currentStatus = connectionStatus.get();
        if (currentStatus == ConnectionStatus.FAILED && retryCount.get() < maxRetryAttempts) {
            log.info("🔄 定期重试MCP连接 (第{}次)", retryCount.get() + 1);
            connectionStatus.set(ConnectionStatus.RECOVERING);
            checkMcpConnectionAsync();
        }
    }
    
    /**
     * 定期健康检查（仅在已连接状态下）
     */
    @Scheduled(fixedRate = 30000)
    public void performHealthCheck() {
        if (!mcpEnabled || connectionStatus.get() != ConnectionStatus.CONNECTED) {
            return;
        }
        
        lastCheckTime.set(LocalDateTime.now());
        
        try {
            if (mcpToolCallbackProvider != null) {
                // 连接的健康检查
                int length = mcpToolCallbackProvider.getToolCallbacks().length;
                log.info("🎯 工具发现完成：发现{}个工具", length);
                log.debug("💚 MCP连接健康检查通过");
            } else {
                log.warn("⚠️ MCP连接健康检查失败: MCP工具提供者不可用");
                connectionStatus.set(ConnectionStatus.FAILED);
                retryCount.set(0); // 重置重试计数以允许重连
            }
        } catch (Exception e) {
            log.warn("⚠️ MCP连接健康检查失败: {}", e.getMessage());
            connectionStatus.set(ConnectionStatus.FAILED);
            retryCount.set(0); // 重置重试计数以允许重连
        }
    }
    
    // Getter方法供其他组件查询状态
    
    public ConnectionStatus getConnectionStatus() {
        return connectionStatus.get();
    }
    
    public boolean isConnected() {
        return connectionStatus.get() == ConnectionStatus.CONNECTED;
    }
    
    public boolean isInitialized() {
        return isInitialized.get();
    }
    
    public int getRetryCount() {
        return retryCount.get();
    }
    
    public String getLastError() {
        return lastError.get();
    }
    
    public LocalDateTime getLastCheckTime() {
        return lastCheckTime.get();
    }
    
    public String getServerUrl() {
        return serverUrl;
    }
    
    public boolean isMcpEnabled() {
        return mcpEnabled;
    }
    
    /**
     * 获取工具数量（真实工具）
     */
    public int getToolCount() {
        if (mcpToolCallbackProvider != null) {
            try {
                return mcpToolCallbackProvider.getToolCallbacks().length;
            } catch (Exception e) {
                log.warn("⚠️ 获取真实工具数量失败: {}", e.getMessage());
                return 0;
            }
        }
        return 0; // 无连接时返回0
    }
    
    /**
     * 获取MCP工具回调提供者
     */
    public SyncMcpToolCallbackProvider getMcpToolCallbackProvider() {
        return mcpToolCallbackProvider;
    }
    
    /**
     * 手动触发重连
     */
    public void manualReconnect() {
        log.info("🔄 手动触发MCP重连...");
        retryCount.set(0);
        connectionStatus.set(ConnectionStatus.CONNECTING);
        checkMcpConnectionAsync();
    }
    
    /**
     * 获取连接详细信息
     */
    public String getConnectionInfo() {
        return String.format("MCP连接状态: %s | 服务器: %s | 工具数量: %d | 重试次数: %d",
            connectionStatus.get().getDisplayText(),
            serverUrl,
            getToolCount(),
            retryCount.get());
    }
} 
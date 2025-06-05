package org.mcp.clientmcp.config;

import io.modelcontextprotocol.client.McpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.customizer.McpSyncClientCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * MCP同步客户端定制器
 * 
 * 用于自定义MCP同步客户端的配置
 */
@Slf4j
@Component
public abstract class McpSyncClientCustomizerImpl implements McpSyncClientCustomizer {

    @Value("${spring.ai.mcp.client.options.connect-timeout:30000}")
    private int connectionTimeoutMs;
    
    @Override
    public void customize(String serverConfigurationName, McpClient.SyncSpec spec) {
        log.info("🔧 自定义MCP同步客户端配置: {}", serverConfigurationName);
        
        // 设置请求超时时间
        spec.requestTimeout(Duration.ofMillis(connectionTimeoutMs));
        
        // 添加工具变更监听器
        spec.toolsChangeConsumer(toolsView -> {
            log.info("🔄 MCP工具列表已更新: {} 个工具可用", toolsView.size());
        });
        
        // 添加根变更监听器
        spec.toolsChangeConsumer(rootsView -> {
            log.info("🔄 MCP根列表已更新: {} 个根可用", rootsView.size());
        });
        
        // 添加提示词变更监听器
        spec.promptsChangeConsumer(promptsView -> {
            log.info("🔄 MCP提示词列表已更新: {} 个提示词可用", promptsView.size());
        });
        
        log.info("✅ MCP同步客户端配置完成: {}", serverConfigurationName);
    }
}
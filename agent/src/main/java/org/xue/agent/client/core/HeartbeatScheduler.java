package org.xue.agent.client.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

/**
 * MCP心跳调度器
 * 只有在mcp.enabled=true且mcp.heartbeat.enabled=true时才启用
 */
@Component
@ConditionalOnProperty(prefix = "mcp", name = "enabled", havingValue = "true", matchIfMissing = true)
public class HeartbeatScheduler implements SchedulingConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(HeartbeatScheduler.class);

    private final ConnectionManager connectionManager;
    private final McpProperties mcpProperties;

    public HeartbeatScheduler(ConnectionManager connectionManager,
                              McpProperties mcpProperties) {
        this.connectionManager = connectionManager;
        this.mcpProperties = mcpProperties;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {
        // 检查心跳是否启用
        if (mcpProperties.getHeartbeat().isEnabled()) {
            logger.info("MCP心跳检查已启用，间隔: {}ms", mcpProperties.getHeartbeat().getInterval());
            registrar.addFixedDelayTask(
                this::runHeartbeat,
                mcpProperties.getHeartbeat().getInterval()
            );
        } else {
            logger.info("MCP心跳检查已禁用");
        }
    }

    private void runHeartbeat() {
        try {
            connectionManager.heartbeatCheck();
        } catch (Exception e) {
            logger.error("执行心跳检查时发生异常: {}", e.getMessage(), e);
        }
    }
}

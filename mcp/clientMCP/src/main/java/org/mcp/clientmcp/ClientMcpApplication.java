package org.mcp.clientmcp;

import lombok.extern.slf4j.Slf4j;
import org.mcp.clientmcp.config.McpConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * MCP客户端应用程序 - 容错优化版本
 * 
 * 🚀 Spring AI MCP客户端特性：
 * - 延迟连接：应用先启动，再异步连接MCP服务器
 * - 故障隔离：MCP服务器故障不影响客户端启动
 * - 自动重试：智能重连机制和健康监控
 * - 优雅降级：服务器不可用时仍可使用客户端其他功能
 * - 完整监控：实时状态监控和详细日志
 * 
 * 📡 连接配置：
 * - 服务器地址: http://localhost:19091
 * - 传输方式: SSE (Server-Sent Events)
 * - 协议版本: MCP 1.0.0
 * - 容错机制: 延迟连接 + 自动重试
 */
@Slf4j
@SpringBootApplication
@EnableScheduling  // 启用定时任务支持重试和健康检查
public class ClientMcpApplication {

    @Autowired
    private Environment environment;
    
    @Autowired
    private McpConnectionManager connectionManager;

    public static void main(String[] args) {
        // 设置启动横幅
        System.setProperty("spring.main.banner-mode", "console");
        
        log.info("🚀 正在启动Spring AI MCP客户端应用程序...");
        log.info("🏠 工作目录: {}", System.getProperty("user.dir"));
        log.info("");
        
        try {
            SpringApplication app = new SpringApplication(ClientMcpApplication.class);
            // 设置应用快速启动，不等待MCP连接
            app.setRegisterShutdownHook(true);
            app.run(args);
        } catch (Exception e) {
            log.error("💥 应用程序启动失败", e);
            System.exit(1);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String port = environment.getProperty("server.port", "19090");
        String baseUrl = "http://localhost:" + port;
        String profile = environment.getProperty("spring.profiles.active", "default");
        
        log.info("🎉 MCP客户端启动成功！");
        log.info("📋 应用程序信息:");
        log.info("   🌐 客户端地址: {}", baseUrl);
        log.info("   📦 运行环境: {}", profile);
        log.info("");
        
        // 显示MCP连接策略说明
        displayConnectionStrategy();
        
        log.info("🔌 可用端点:");
        log.info("   🧪 测试端点: {}/api/mcp/test", baseUrl);
        log.info("   🤝 协议握手: {}/api/mcp/handshake", baseUrl);
        log.info("   🔍 工具发现: {}/api/mcp/tools/discover", baseUrl);
        log.info("   🚀 工具调用: {}/api/mcp/tools/call", baseUrl);
        log.info("   🔗 连接状态: {}/api/mcp/connection/status", baseUrl);
        log.info("   📊 综合状态: {}/api/mcp/status/complete", baseUrl);
        log.info("   🏥 健康检查: {}/api/mcp/health", baseUrl);
        log.info("");
        
        printQuickStartGuide(baseUrl);
        
        log.info("✅ MCP客户端应用已就绪，异步加载MCP Server，等待API调用...");
        log.info("");
    }
    
    /**
     * 显示连接策略说明
     */
    private void displayConnectionStrategy() {
        log.info("🛡️ MCP连接策略:");
        
        if (connectionManager.isMcpEnabled()) {
            // 从配置读取重试次数
            String maxAttempts = environment.getProperty("spring.ai.mcp.client.sse.connections.file-server.retry.max-attempts", "5");
            String retryInterval = environment.getProperty("spring.ai.mcp.client.sse.connections.file-server.retry.max-delay", "10s");
            String healthCheckInterval = environment.getProperty("spring.ai.mcp.client.sse.connections.file-server.connection-timeout", "60s");
            
            log.info("   📡 连接模式: 延迟连接 (应用启动后异步连接)");
            log.info("   🎯 目标服务器: {}", connectionManager.getServerUrl());
            log.info("   🔄 重试机制: 自动重试，最多{}次，间隔{}", maxAttempts, retryInterval);
            log.info("   💚 健康检查: 连接超时{}", healthCheckInterval);
            log.info("");
            log.info("   📊 当前状态: {}", connectionManager.getConnectionStatus().getDisplayText());
            
            if (connectionManager.getConnectionStatus() == McpConnectionManager.ConnectionStatus.DISCONNECTED) {
                log.info("   🔍 说明: MCP连接初始化正在后台进行...");
            }
        } else {
            log.info("   🚫 MCP客户端已禁用");
        }
        
        log.info("");
    }
    
    /**
     * 打印快速开始指南
     */
    private void printQuickStartGuide(String baseUrl) {
        log.info("📚 快速开始指南:");
        log.info("   1️⃣ 检查连接: curl {}/api/mcp/handshake", baseUrl);
        log.info("   2️⃣ 发现工具: curl {}/api/mcp/tools/discover", baseUrl);
        log.info("   3️⃣ 测试调用: curl -X POST \"{}/api/mcp/tools/call?toolName=readFile\" \\", baseUrl);
        log.info("                    -H \"Content-Type: application/json\" \\");
        log.info("                    -d '{\"filePath\":\"/path/to/file.txt\"}'");
        log.info("   4️⃣ 综合状态: curl {}/api/mcp/status/complete", baseUrl);
        log.info("   5️⃣ 连接详情: curl {}/api/mcp/connection/status", baseUrl);
        log.info("");
        log.info("🔧 故障排除:");
        log.info("   • 客户端会在后台自动尝试连接MCP服务器");
        log.info("   • 即使MCP服务器不可用，客户端API仍然可以访问");
        log.info("   • 使用连接状态端点监控MCP服务器连接情况");
        log.info("   • 支持手动重连和自动恢复机制");
        log.info("   • 查看应用日志获取详细的连接状态信息");
        log.info("");
    }
} 
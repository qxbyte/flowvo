package org.xue.mcp_client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.xue.mcp_client.core.McpClientTemplate;

/**
 * MCP客户端应用程序
 */
@SpringBootApplication
@EnableScheduling
public class McpClientApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(McpClientApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(McpClientApplication.class, args);
        
        // 从环境中获取配置
        Environment env = context.getEnvironment();
        String port = env.getProperty("server.port", "8081");
        String contextPath = env.getProperty("server.servlet.context-path", "/");
        
        // 打印启动信息
        logger.info("MCP客户端已启动, 访问地址: http://localhost:{}{}", port, contextPath);
        
        // 检查服务是否已注册
        McpClientTemplate mcpTemplate = context.getBean(McpClientTemplate.class);
        if (mcpTemplate != null) {
            logger.info("MCP客户端已成功注册!");
            
            // 获取并打印所有服务状态
            var serversStatus = mcpTemplate.getServersStatus();
            if (serversStatus.isEmpty()) {
                logger.warn("未配置任何MCP服务");
            } else {
                logger.info("MCP服务状态: {}", serversStatus);
            }
        }
    }
}

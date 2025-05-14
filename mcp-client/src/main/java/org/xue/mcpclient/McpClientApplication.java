package org.xue.mcpclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.xue.mcpclient.service.MCPDatabaseServiceWrapper;

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
        String mysqlUrl = env.getProperty("mcp.mysql.url", "http://localhost:8082");
        
        // 打印启动信息
        logger.info("MCP Common服务已启动, 访问地址: http://localhost:{}{}", port, contextPath);
        logger.info("测试接口地址: http://localhost:{}/api/test/status", port);
        logger.info("MCP MySQL服务地址: {}", mysqlUrl);
        
        // 检查服务是否已注册
        MCPDatabaseServiceWrapper mcpService = context.getBean(MCPDatabaseServiceWrapper.class);
        if (mcpService != null) {
            logger.info("MCP数据库服务客户端已成功注册!");
            
            try {
                // 尝试获取数据库元数据
                var metadata = mcpService.getDatabaseMetadata();
                if (metadata.containsKey("error")) {
                    logger.warn("远程MCP数据库服务当前不可用: {}", metadata.get("error"));
                    logger.info("服务将使用降级模式运行，并在远程服务可用时自动连接");
                } else {
                    logger.info("已成功连接到MCP数据库服务: {}", metadata);
                }
            } catch (Exception e) {
                logger.warn("连接MCP数据库服务失败: {}", e.getMessage());
            }
        }
    }
}

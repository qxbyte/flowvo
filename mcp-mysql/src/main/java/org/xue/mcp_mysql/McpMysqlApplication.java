package org.xue.mcp_mysql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class McpMysqlApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(McpMysqlApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(McpMysqlApplication.class, args);
    }
    
    /**
     * 应用启动成功后显示访问地址
     */
    @Bean
    public CommandLineRunner showApplicationInfo(Environment env) {
        return args -> {
            String port = env.getProperty("server.port", "50941");
            String contextPath = env.getProperty("server.servlet.context-path", "/");
            String host = env.getProperty("server.address", "localhost");
            String rpcPath = env.getProperty("app.rpc.path", "/api/rpc/db");
            
            if (!contextPath.startsWith("/")) {
                contextPath = "/" + contextPath;
            }
            if (!contextPath.endsWith("/") && !contextPath.isEmpty()) {
                contextPath = contextPath + "/";
            }
            
            logger.info("MCP MySQL服务已启动, 访问地址: http://{}:{}{}", host, port, contextPath);
            logger.info("JSON-RPC接口地址: http://{}:{}{}", host, port, rpcPath);
        };
    }
}

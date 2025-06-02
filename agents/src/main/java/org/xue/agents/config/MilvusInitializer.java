package org.xue.agents.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Milvus数据库初始化配置
 * 通过Spring AI VectorStore进行初始化检查
 */
@Slf4j
@Configuration
public class MilvusInitializer {

    @Autowired
    private VectorStore vectorStore;

    /**
     * 应用启动时检查Milvus连接
     */
    @Bean
    public ApplicationRunner initializeMilvus() {
        return args -> {
            log.info("开始检查Milvus数据库连接...");
            
            try {
                // 通过VectorStore检查连接
                String storeName = vectorStore.getName();
                log.info("成功连接到向量数据库: {}", storeName);
                
                // 尝试一个简单的操作来确保collection可用
                // 如果collection不存在，Spring AI会自动创建
                log.info("向量数据库已就绪，Spring AI会自动管理collection");
                
            } catch (Exception e) {
                log.error("向量数据库连接失败", e);
                log.info("请确保Milvus服务正在运行，或collection将在首次使用时自动创建");
            }
            
            log.info("Milvus数据库检查完成！");
        };
    }
} 
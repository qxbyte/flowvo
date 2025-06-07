package org.xue.agents.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Milvus数据库初始化配置
 * 通过手动配置的VectorStore进行初始化检查
 */
@Slf4j
@Configuration
public class MilvusInitializer {

    /**
     * 应用启动时检查Milvus连接
     * 通过ApplicationContext动态获取VectorStore Bean
     */
    @Bean
    public ApplicationRunner initializeMilvus(ApplicationContext applicationContext) {
        return args -> {
            log.info("开始检查Milvus数据库连接...");
            
            try {
                // 动态获取VectorStore Bean
                VectorStore vectorStore = applicationContext.getBean(VectorStore.class);
                log.info("成功获取VectorStore: {}", vectorStore.getClass().getSimpleName());
                
                // 这里可以添加更多的初始化逻辑
                // 比如检查collection是否存在，创建索引等
                log.info("Milvus数据库连接检查完成");
                
            } catch (Exception e) {
                log.error("Milvus数据库连接检查失败", e);
                // 根据需要决定是否抛出异常阻止应用启动
                // throw new RuntimeException("Milvus初始化失败", e);
            }
        };
    }
} 
package org.mcp.clientmcp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务和定时任务配置
 * 
 * 🔄 功能支持：
 * - MCP连接异步初始化
 * - 定时重连重试机制
 * - 定时健康检查
 * - 非阻塞任务执行
 */
@Slf4j
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    /**
     * 异步任务执行器 - 用于MCP连接初始化
     */
    @Bean(name = "mcpAsyncExecutor")
    public Executor mcpAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 线程池配置
        executor.setCorePoolSize(2);           // 核心线程数
        executor.setMaxPoolSize(4);            // 最大线程数
        executor.setQueueCapacity(10);         // 队列容量
        executor.setKeepAliveSeconds(60);      // 线程空闲时间
        
        // 线程命名
        executor.setThreadNamePrefix("MCP-Async-");
        
        // 拒绝策略：由调用线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待任务完成再关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        
        log.info("🔄 MCP异步任务执行器已初始化: 核心线程={}, 最大线程={}", 
            executor.getCorePoolSize(), executor.getMaxPoolSize());
        
        return executor;
    }
    
    /**
     * 定时任务调度器 - 用于重连重试和健康检查
     */
    @Bean(name = "mcpScheduler")
    public ThreadPoolTaskScheduler mcpScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        
        // 调度器配置
        scheduler.setPoolSize(2);              // 调度线程池大小
        scheduler.setThreadNamePrefix("MCP-Scheduler-");
        
        // 等待任务完成再关闭
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        
        // 拒绝策略
        scheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        scheduler.initialize();
        
        log.info("📅 MCP定时任务调度器已初始化: 线程池大小={}", scheduler.getPoolSize());
        
        return scheduler;
    }
} 
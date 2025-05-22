package org.xue.app.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Eureka客户端配置
 * 显式禁用Eureka客户端功能
 */
@Configuration
@EnableDiscoveryClient(autoRegister = false)
public class EurekaConfig {
    // 空实现，只为禁用Eureka自动注册
} 
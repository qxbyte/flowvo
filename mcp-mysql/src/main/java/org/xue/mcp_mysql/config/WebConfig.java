package org.xue.mcp_mysql.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.Collections;

/**
 * Web配置
 * 配置跨域资源共享(CORS)支持
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);
    
    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;
    
    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;
    
    @Value("${cors.allowed-headers:*}")
    private String allowedHeaders;
    
    @Value("${cors.max-age:3600}")
    private long maxAge;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        logger.info("配置CORS映射...");
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders("Content-Type", "Authorization", "Accept", "X-Requested-With")
                .allowCredentials(false)
                .maxAge(maxAge);
    }
    
    /**
     * 配置全局CORS过滤器
     * 确保所有请求都支持CORS
     */
    @Bean
    public CorsFilter corsFilter() {
        logger.info("配置CORS过滤器...");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许所有来源
        config.setAllowedOriginPatterns(Collections.singletonList("*"));
        
        // 允许所有请求头
        config.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization", 
                "X-Requested-With", "Access-Control-Allow-Origin", "Access-Control-Allow-Headers"));
        
        // 允许所有HTTP方法
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        
        // 允许暴露的响应头
        config.setExposedHeaders(Arrays.asList("Content-Type", "Authorization", "Accept"));
        
        // 不允许发送认证信息
        config.setAllowCredentials(false);
        
        // 预检请求的有效期，单位秒
        config.setMaxAge(maxAge);
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
} 
package org.xue.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web配置
 * 配置跨域资源共享(CORS)支持
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);
    
    @Value("${app.upload.avatar-dir:./uploads/avatars/}")
    private String avatarUploadDir;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        logger.info("配置CORS映射...");
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders("Content-Type", "Authorization", "Accept", "X-Requested-With")
                .allowCredentials(false)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置头像图片的静态资源访问
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations("file:" + avatarUploadDir);
    }
} 
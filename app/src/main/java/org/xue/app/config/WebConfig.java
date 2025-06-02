package org.xue.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;

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
        // 处理相对路径，转换为绝对路径
        String absoluteAvatarDir;
        if (avatarUploadDir.startsWith("./")) {
            // 获取当前工作目录（应该是项目根目录）
            String workingDir = System.getProperty("user.dir");
            logger.info("当前工作目录: {}", workingDir);
            
            // 如果工作目录是app子目录，需要返回到项目根目录
            if (workingDir.endsWith("/app") || workingDir.endsWith("\\app")) {
                workingDir = new File(workingDir).getParent();
                logger.info("调整工作目录到项目根目录: {}", workingDir);
            }
            
            absoluteAvatarDir = Paths.get(workingDir, avatarUploadDir.substring(2)).toString() + File.separator;
        } else {
            absoluteAvatarDir = avatarUploadDir;
        }
        
        logger.info("配置头像静态资源访问路径: {}", absoluteAvatarDir);
        
        // 配置头像图片的静态资源访问
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations("file:" + absoluteAvatarDir);
    }
} 
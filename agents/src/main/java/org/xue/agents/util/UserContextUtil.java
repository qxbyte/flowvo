package org.xue.agents.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 用户上下文工具类
 * 从网关传递的请求头中提取用户信息
 */
public class UserContextUtil {
    
    /**
     * 获取当前请求的用户名
     */
    public static String getCurrentUsername() {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            return request.getHeader("X-User-Name");
        }
        return null;
    }
    
    /**
     * 获取当前请求的用户ID
     */
    public static String getCurrentUserId() {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            return request.getHeader("X-User-Id");
        }
        return null;
    }
    
    /**
     * 检查当前请求是否已认证
     */
    public static boolean isAuthenticated() {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            String tokenValid = request.getHeader("X-Token-Valid");
            return "true".equals(tokenValid);
        }
        return false;
    }
    
    /**
     * 获取当前HTTP请求
     */
    private static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
    
    /**
     * 获取用户信息摘要（用于日志）
     */
    public static String getUserSummary() {
        String username = getCurrentUsername();
        String userId = getCurrentUserId();
        boolean authenticated = isAuthenticated();
        
        if (username != null && authenticated) {
            return String.format("User{id=%s, name=%s}", userId, username);
        }
        return "Anonymous";
    }
} 
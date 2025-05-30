package org.xue.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.xue.app.dto.AuthResponse;
import org.xue.app.entity.User;

/**
 * 基础控制器，提供通用方法
 */
public abstract class BaseController {
    
    protected final Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * 获取当前登录用户的ID
     * 
     * @return 用户ID，如果未登录则返回null
     */
    protected String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            // 根据认证对象的类型获取用户ID
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof User) {
                // 如果是User对象，直接获取ID
                return ((User) principal).getId().toString();
            } else if (principal instanceof String) {
                // 如果是字符串（用户名或ID），直接返回
                return (String) principal;
            } else if (principal instanceof org.springframework.security.core.userdetails.User) {
                // 如果是Spring Security的User对象，返回用户名
                return ((org.springframework.security.core.userdetails.User) principal).getUsername();
            }
            
            // 返回认证名称作为后备选项
            return authentication.getName();
        }
        
        // 未登录或认证失败
        return null;
    }
    
    /**
     * 在所有控制器方法前调用，日志记录当前用户
     */
    @ModelAttribute
    public void logCurrentUser() {
        String userId = getCurrentUserId();
        log.debug("当前请求用户ID: {}", userId != null ? userId : "未登录");
    }
} 
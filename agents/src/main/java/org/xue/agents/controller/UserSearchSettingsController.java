package org.xue.agents.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.xue.agents.entity.UserSearchSettings;
import org.xue.agents.service.UserSearchSettingsService;

/**
 * 用户搜索设置控制器
 */
@RestController
@RequestMapping("/api/search-settings")
@RequiredArgsConstructor
@Slf4j
public class UserSearchSettingsController {
    
    private final UserSearchSettingsService userSearchSettingsService;
    
    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("用户未登录");
        }
        
        return authentication.getName();
    }
    
    /**
     * 获取用户搜索设置
     */
    @GetMapping
    public ResponseEntity<UserSearchSettings> getUserSettings() {
        try {
            String currentUserId = getCurrentUserId();
            log.info("获取用户搜索设置: userId={}", currentUserId);
            
            UserSearchSettings settings = userSearchSettingsService.getUserSettings(currentUserId);
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            log.error("获取用户搜索设置失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 保存用户搜索设置
     */
    @PostMapping
    public ResponseEntity<?> saveUserSettings(@RequestBody UserSearchSettings settings) {
        try {
            String currentUserId = getCurrentUserId();
            log.info("保存用户搜索设置: userId={}, settings={}", currentUserId, settings);
            
            UserSearchSettings saved = userSearchSettingsService.saveUserSettings(currentUserId, settings);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            log.warn("保存用户搜索设置参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("保存用户搜索设置失败", e);
            return ResponseEntity.internalServerError().body("保存设置失败，请稍后重试");
        }
    }
    
    /**
     * 更新用户搜索设置
     */
    @PutMapping
    public ResponseEntity<?> updateUserSettings(@RequestBody UserSearchSettings settings) {
        try {
            String currentUserId = getCurrentUserId();
            log.info("更新用户搜索设置: userId={}, settings={}", currentUserId, settings);
            
            UserSearchSettings saved = userSearchSettingsService.saveUserSettings(currentUserId, settings);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            log.warn("更新用户搜索设置参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("更新用户搜索设置失败", e);
            return ResponseEntity.internalServerError().body("更新设置失败，请稍后重试");
        }
    }
    
    /**
     * 重置用户搜索设置为默认值
     */
    @PostMapping("/reset")
    public ResponseEntity<?> resetUserSettings() {
        try {
            String currentUserId = getCurrentUserId();
            log.info("重置用户搜索设置: userId={}", currentUserId);
            
            UserSearchSettings settings = userSearchSettingsService.resetUserSettings(currentUserId);
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            log.error("重置用户搜索设置失败", e);
            return ResponseEntity.internalServerError().body("重置设置失败，请稍后重试");
        }
    }
    
    /**
     * 删除用户搜索设置
     */
    @DeleteMapping
    public ResponseEntity<String> deleteUserSettings() {
        try {
            String currentUserId = getCurrentUserId();
            log.info("删除用户搜索设置: userId={}", currentUserId);
            
            userSearchSettingsService.deleteUserSettings(currentUserId);
            return ResponseEntity.ok("设置删除成功");
        } catch (Exception e) {
            log.error("删除用户搜索设置失败", e);
            return ResponseEntity.internalServerError().body("删除设置失败，请稍后重试");
        }
    }
    
    /**
     * 获取默认搜索设置
     */
    @GetMapping("/defaults")
    public ResponseEntity<UserSearchSettings> getDefaultSettings() {
        try {
            log.debug("获取默认搜索设置");
            UserSearchSettings defaultSettings = userSearchSettingsService.getDefaultSettings();
            return ResponseEntity.ok(defaultSettings);
        } catch (Exception e) {
            log.error("获取默认搜索设置失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 
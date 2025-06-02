package org.xue.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xue.app.dto.UserSettingsDTO;
import org.xue.app.entity.User;
import org.xue.app.service.UserSettingsService;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户设置控制器
 */
@RestController
@RequestMapping("/api/user/settings")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserSettingsController {

    @Autowired
    private UserSettingsService userSettingsService;

    /**
     * 获取用户设置信息
     */
    @GetMapping
    public ResponseEntity<?> getUserSettings() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        String currentUsername = authentication.getName();

        UserSettingsDTO settings = userSettingsService.getUserSettings(currentUsername);
        if (settings != null) {
            return ResponseEntity.ok(settings);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User settings not found");
        }
    }

    /**
     * 更新用户设置信息
     */
    @PostMapping
    public ResponseEntity<?> updateUserSettings(@RequestBody UserSettingsDTO settingsDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        String currentUsername = authentication.getName();

        try {
            User updatedUser = userSettingsService.updateUserSettings(currentUsername, settingsDTO);
            if (updatedUser != null) {
                UserSettingsDTO updatedSettings = new UserSettingsDTO();
                updatedSettings.setUsername(updatedUser.getUsername());
                updatedSettings.setNickname(updatedUser.getNickname());
                updatedSettings.setEmail(updatedUser.getEmail());
                updatedSettings.setAvatarUrl(updatedUser.getAvatarUrl());
                return ResponseEntity.ok(updatedSettings);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update user settings");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * 更新用户昵称
     */
    @PostMapping("/nickname")
    public ResponseEntity<?> updateNickname(@RequestBody Map<String, String> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        String currentUsername = authentication.getName();
        String nickname = request.get("nickname");

        if (nickname == null || nickname.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("昵称不能为空");
        }

        boolean success = userSettingsService.updateNickname(currentUsername, nickname);
        if (success) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "昵称更新成功");
            response.put("nickname", nickname);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("昵称更新失败");
        }
    }

    /**
     * 更新用户邮箱
     */
    @PostMapping("/email")
    public ResponseEntity<?> updateEmail(@RequestBody Map<String, String> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        String currentUsername = authentication.getName();
        String email = request.get("email");

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("邮箱不能为空");
        }

        boolean success = userSettingsService.updateEmail(currentUsername, email);
        if (success) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "邮箱更新成功");
            response.put("email", email);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("邮箱更新失败");
        }
    }

    /**
     * 更新用户密码
     */
    @PostMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        String currentUsername = authentication.getName();
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");
        String confirmPassword = request.get("confirmPassword");

        if (currentPassword == null || currentPassword.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("当前密码不能为空");
        }

        if (newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("新密码不能为空");
        }

        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("新密码与确认密码不匹配");
        }

        if (newPassword.length() < 6) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("新密码长度至少为6位");
        }

        boolean success = userSettingsService.updatePassword(currentUsername, currentPassword, newPassword);
        if (success) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "密码更新成功");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("当前密码不正确");
        }
    }

    /**
     * 上传用户头像
     */
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        String currentUsername = authentication.getName();

        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("请选择要上传的文件");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("只能上传图片文件");
        }

        // 检查文件大小 (限制为5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("文件大小不能超过5MB");
        }

        try {
            String avatarUrl = userSettingsService.uploadAvatar(currentUsername, file);
            Map<String, String> response = new HashMap<>();
            response.put("message", "头像上传成功");
            response.put("avatarUrl", avatarUrl);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * 验证当前密码
     */
    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyPassword(@RequestBody Map<String, String> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        String currentUsername = authentication.getName();
        String password = request.get("password");

        boolean isValid = userSettingsService.verifyCurrentPassword(currentUsername, password);
        Map<String, Boolean> response = new HashMap<>();
        response.put("valid", isValid);
        return ResponseEntity.ok(response);
    }
}

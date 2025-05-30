package org.xue.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.xue.app.dto.UserSettingsDTO;
import org.xue.app.entity.User;
import org.xue.app.service.UserSettingsService;

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
        String currentUserId = authentication.getName(); // Assuming username is used as userId

        UserSettingsDTO settings = userSettingsService.getUserSettings(currentUserId);
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
        String currentUserId = authentication.getName(); // Assuming username is used as userId

        User updatedUser = userSettingsService.updateUserSettings(currentUserId, settingsDTO);
        if (updatedUser != null) {
            // Optionally return the updated user details (excluding password)
            UserSettingsDTO updatedSettings = new UserSettingsDTO();
            updatedSettings.setUsername(updatedUser.getUsername());
            updatedSettings.setEmail(updatedUser.getEmail());
            return ResponseEntity.ok(updatedSettings);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update user settings");
        }
    }
}

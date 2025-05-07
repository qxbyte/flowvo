package org.xue.chat.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xue.chat.entity.User;
import org.xue.chat.service.UserService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class AuthController {
    
    private final UserService userService;
    
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        log.info("收到登录请求: {}", loginRequest.get("username")); // 添加日志
        
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        
        if (userService.validateUser(username, password)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            return ResponseEntity.ok(response);
        }
        
        return ResponseEntity.badRequest().body("用户名或密码错误");
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        log.info("收到注册请求: {}", user.getUsername()); // 添加日志
        user.setRole("ROLE_USER");
        try {
            User savedUser = userService.saveUser(user);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("userId", savedUser.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("注册失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("注册失败: " + e.getMessage());
        }
    }
}
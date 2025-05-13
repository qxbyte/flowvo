package org.xue.core.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.xue.core.entity.User;
import org.xue.core.service.UserService;
import org.xue.core.security.JwtService;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    private final UserService userService;
    private final JwtService jwtService;
    
    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        log.info("收到登录请求: {}", loginRequest.get("username")); // 添加日志
        
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        
        if (username == null || password == null) {
            log.error("登录失败: 用户名或密码为空");
            return ResponseEntity.badRequest().body("用户名和密码不能为空");
        }
        
        try {
            // 直接使用UserService验证用户凭据，而不是使用AuthenticationManager
            if (!userService.validateUser(username, password)) {
                log.error("登录失败: 用户名或密码错误");
                return ResponseEntity.status(401).body("用户名或密码错误");
            }
            
            // 获取用户信息
            User user = userService.getUserByUsername(username);
            
            // 创建UserDetails对象用于生成token
            UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
            );
            
            // 生成JWT
            String token = jwtService.generateToken(userDetails);
            
            // 返回响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("token", token);
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage(), e);
            return ResponseEntity.status(401).body("用户名或密码错误");
        }
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
    
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser() {
        // 从安全上下文中获取当前认证用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            
            return ResponseEntity.ok(response);
        }
        
        return ResponseEntity.status(401).body("未认证的用户");
    }
}
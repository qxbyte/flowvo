package org.xue.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.xue.app.dto.AuthResponse;
import org.xue.app.dto.LoginRequest;
import org.xue.app.dto.LoginResponse;
import org.xue.app.dto.RegisterRequest;
import org.xue.app.service.AuthService;

/**
 * 身份认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * 登录接口
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 注册接口
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            AuthResponse response = authService.getCurrentUser(username);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.ok(AuthResponse.failure("未登录"));
    }
    
    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout() {
        // JWT是无状态的，客户端需要自行删除token
        return ResponseEntity.ok(AuthResponse.success(null, null));
    }
    
    /**
     * 验证token有效性
     */
    @GetMapping("/validate")
    public ResponseEntity<Void> validateToken() {
        // 如果能到达这个方法，说明过滤器已经验证了token
        return ResponseEntity.ok().build();
    }
} 
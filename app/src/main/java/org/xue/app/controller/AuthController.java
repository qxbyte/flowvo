package org.xue.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xue.app.dto.LoginRequest;
import org.xue.app.dto.LoginResponse;
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
     * 验证token有效性
     */
    @GetMapping("/validate")
    public ResponseEntity<Void> validateToken() {
        // 如果能到达这个方法，说明过滤器已经验证了token
        return ResponseEntity.ok().build();
    }
} 
package org.xue.app.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.xue.app.dto.LoginRequest;
import org.xue.app.dto.LoginResponse;
import org.xue.app.security.JwtService;
import org.xue.app.service.AuthService;

import java.util.ArrayList;

/**
 * 认证服务实现类
 */
@Service
public class AuthServiceImpl implements AuthService {
    
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    
    private final JwtService jwtService;
    
    public AuthServiceImpl(JwtService jwtService) {
        this.jwtService = jwtService;
    }
    
    @Override
    public LoginResponse login(LoginRequest request) {
        // 简化处理，只要用户名和密码不为空就认为验证通过
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return LoginResponse.failure("用户名不能为空");
        }
        
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return LoginResponse.failure("密码不能为空");
        }
        
        // 创建UserDetails对象
        UserDetails userDetails = User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .authorities(new ArrayList<>())
                .build();
        
        // 生成令牌
        String token = jwtService.generateToken(userDetails);
        
        return LoginResponse.success(token, request.getUsername());
    }
} 
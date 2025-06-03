package org.xue.app.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.xue.app.dto.*;
import org.xue.app.entity.User;
import org.xue.app.repository.UserRepository;
import org.xue.app.security.JwtService;
import org.xue.app.service.AuthService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务实现类
 */
@Service
public class AuthServiceImpl implements AuthService {
    
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public AuthServiceImpl(JwtService jwtService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public LoginResponse login(LoginRequest request) {
        // 验证请求参数
        if ((request.getUsername() == null || request.getUsername().trim().isEmpty()) && 
            (request.getEmail() == null || request.getEmail().trim().isEmpty())) {
            return LoginResponse.failure("请输入用户名或邮箱");
        }
        
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return LoginResponse.failure("请输入密码");
        }
        
        try {
            // 查找用户（支持用户名或邮箱登录）
            Optional<User> userOptional = Optional.empty();
            
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                userOptional = userRepository.findByEmail(request.getEmail());
            } else if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
                userOptional = userRepository.findByUsername(request.getUsername());
            }
            
            if (userOptional.isEmpty()) {
                return LoginResponse.failure("用户名或密码不正确");
            }
            
            User user = userOptional.get();
            
            // 验证密码
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return LoginResponse.failure("用户名或密码不正确");
            }
            
            // 创建UserDetails对象
            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .authorities(Collections.singletonList(new SimpleGrantedAuthority(user.getRole())))
                    .build();
            
            // 创建额外的claims，包含用户ID
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("userId", user.getId().toString());
            
            log.info("=== JWT生成调试信息 ===");
            log.info("用户数据库ID: {}", user.getId());
            log.info("用户名: {}", user.getUsername());
            log.info("添加到JWT的userId: {}", user.getId().toString());
            log.info("extraClaims内容: {}", extraClaims);
            
            // 生成包含用户ID的令牌
            String token = jwtService.generateToken(extraClaims, userDetails);
            
            log.info("生成的JWT token: {}", token);
            log.info("=====================");
            
            // 构建用户信息并返回
            UserInfoDTO userInfo = UserInfoDTO.builder()
                    .id(user.getId().toString())
                    .username(user.getUsername())
                    .name(user.getNickname() != null ? user.getNickname() : user.getUsername())
                    .email(user.getEmail())
                    .avatar(user.getAvatarUrl())
                    .roles(Collections.singletonList(user.getRole()))
                    .build();
            
            return LoginResponse.success(token, userInfo);
        } catch (Exception e) {
            log.error("登录失败", e);
            return LoginResponse.failure("登录失败: " + e.getMessage());
        }
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        // 验证请求
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return AuthResponse.failure("用户名不能为空");
        }
        
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return AuthResponse.failure("密码不能为空");
        }
        
        // 检查用户名是否已存在
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return AuthResponse.failure("用户名已存在");
        }
        
        try {
            // 创建新用户
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            
            // 处理昵称字段，确保不为null
            String nickname = request.getNickname();
            if (nickname == null || nickname.trim().isEmpty()) {
                nickname = request.getName(); // 尝试使用name字段
                if (nickname == null || nickname.trim().isEmpty()) {
                    nickname = request.getUsername(); // 最后使用username
                }
            }
            user.setNickname(nickname);
            
            user.setEmail(request.getEmail() != null ? request.getEmail() : request.getUsername() + "@example.com");
            user.setRole("USER");
            
            log.info("注册新用户: username={}, nickname={}, email={}", 
                user.getUsername(), user.getNickname(), user.getEmail());
            
            // 保存用户
            User savedUser = userRepository.save(user);
            
            // 创建用户详情
            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(savedUser.getUsername())
                    .password(savedUser.getPassword())
                    .authorities(new ArrayList<>())
                    .build();
            
            // 创建额外的claims，包含用户ID
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("userId", savedUser.getId().toString());
            
            // 生成包含用户ID的令牌
            String token = jwtService.generateToken(extraClaims, userDetails);
            
            // 构建用户信息
            UserInfoDTO userInfo = UserInfoDTO.builder()
                    .id(savedUser.getId().toString())
                    .username(savedUser.getUsername())
                    .name(savedUser.getNickname())
                    .email(savedUser.getEmail())
                    .avatar(savedUser.getAvatarUrl())
                    .roles(Collections.singletonList("USER"))
                    .build();
            
            return AuthResponse.success(token, userInfo);
        } catch (Exception e) {
            log.error("注册失败", e);
            return AuthResponse.failure("注册失败: " + e.getMessage());
        }
    }

    @Override
    public AuthResponse getCurrentUser(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // 构建用户信息
            UserInfoDTO userInfo = UserInfoDTO.builder()
                    .id(user.getId().toString())
                    .username(user.getUsername())
                    .name(user.getNickname())
                    .email(user.getEmail())
                    .avatar(user.getAvatarUrl())
                    .roles(Collections.singletonList(user.getRole()))
                    .build();
            
            return AuthResponse.success(null, userInfo);
        }
        
        return AuthResponse.failure("用户不存在");
    }

    @Override
    public AuthResponse checkEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return AuthResponse.failure("邮箱不能为空");
        }
        
        try {
            // 检查邮箱是否已存在
            Optional<User> userOptional = userRepository.findByEmail(email);
            
            if (userOptional.isPresent()) {
                // 用户已存在，返回用户信息用于登录流程
                User user = userOptional.get();
                UserInfoDTO userInfo = UserInfoDTO.builder()
                        .id(user.getId().toString())
                        .username(user.getUsername())
                        .name(user.getNickname())
                        .email(user.getEmail())
                        .avatar(user.getAvatarUrl())
                        .roles(Collections.singletonList(user.getRole()))
                        .build();
                
                return AuthResponse.builder()
                        .success(true)
                        .message("用户已存在")
                        .userInfo(userInfo)
                        .token(null)
                        .build();
            } else {
                // 用户不存在，可以进入注册流程
                return AuthResponse.builder()
                        .success(true)
                        .message("用户不存在，可以注册")
                        .userInfo(null)
                        .token(null)
                        .build();
            }
        } catch (Exception e) {
            log.error("检查邮箱失败", e);
            return AuthResponse.failure("检查邮箱失败，请稍后重试");
        }
    }
} 
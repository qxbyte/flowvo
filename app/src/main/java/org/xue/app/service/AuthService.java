package org.xue.app.service;

import org.xue.app.dto.AuthResponse;
import org.xue.app.dto.LoginRequest;
import org.xue.app.dto.LoginResponse;
import org.xue.app.dto.RegisterRequest;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 注册响应
     */
    AuthResponse register(RegisterRequest request);

    /**
     * 获取当前用户信息
     *
     * @param username 用户名
     * @return 用户信息响应
     */
    AuthResponse getCurrentUser(String username);

    /**
     * 检查邮箱是否已存在
     *
     * @param email 邮箱地址
     * @return 检查结果响应
     */
    AuthResponse checkEmail(String email);
    String getCurrentUserId(String username);
} 
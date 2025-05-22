package org.xue.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String username;
    private String message;
    
    /**
     * 创建成功的登录响应
     */
    public static LoginResponse success(String token, String username) {
        return LoginResponse.builder()
                .token(token)
                .username(username)
                .message("登录成功")
                .build();
    }
    
    /**
     * 创建失败的登录响应
     */
    public static LoginResponse failure(String message) {
        return LoginResponse.builder()
                .message(message)
                .build();
    }
} 
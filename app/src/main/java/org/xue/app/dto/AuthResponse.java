package org.xue.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserInfoDTO userInfo;
    private boolean success;
    private String message;
    
    public static AuthResponse success(String token, UserInfoDTO userInfo) {
        return AuthResponse.builder()
                .token(token)
                .userInfo(userInfo)
                .success(true)
                .message("认证成功")
                .build();
    }
    
    public static AuthResponse failure(String message) {
        return AuthResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
} 
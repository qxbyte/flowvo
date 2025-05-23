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
    private boolean success;
    private String message;
    private String token;
    private UserInfoDTO userInfo;
    
    /**
     * 创建成功的登录响应
     */
    public static LoginResponse success(String token, UserInfoDTO userInfo) {
        return LoginResponse.builder()
                .success(true)
                .message("登录成功")
                .token(token)
                .userInfo(userInfo)
                .build();
    }
    
    /**
     * 创建失败的登录响应
     */
    public static LoginResponse failure(String message) {
        return LoginResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
} 
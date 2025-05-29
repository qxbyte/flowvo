package org.xue.app.dto;

import lombok.Data;

/**
 * 注册请求DTO
 */
@Data
public class RegisterRequest {
    private String username;
    private String nickname;
    private String password;
    private String email;
    private String name;
} 
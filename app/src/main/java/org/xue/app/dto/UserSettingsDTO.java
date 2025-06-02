package org.xue.app.dto;

import lombok.Data;

@Data
public class UserSettingsDTO {
    private String username;
    private String nickname;
    private String email;
    private String password; // For changing password
    private String currentPassword; // For password verification
    private String confirmPassword; // For password confirmation
    private String avatarUrl;
}

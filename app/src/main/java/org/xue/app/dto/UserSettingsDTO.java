package org.xue.app.dto;

import lombok.Data;

@Data
public class UserSettingsDTO {
    private String username;
    private String email;
    private String password; // For changing password
}

package org.xue.app.service;

import org.springframework.web.multipart.MultipartFile;
import org.xue.app.dto.UserSettingsDTO;
import org.xue.app.entity.User;

public interface UserSettingsService {
    UserSettingsDTO getUserSettings(String userId);
    User updateUserSettings(String userId, UserSettingsDTO userSettingsDTO);
    boolean updateNickname(String userId, String nickname);
    boolean updateEmail(String userId, String email);
    boolean updatePassword(String userId, String currentPassword, String newPassword);
    String uploadAvatar(String userId, MultipartFile file);
    boolean verifyCurrentPassword(String userId, String currentPassword);
}

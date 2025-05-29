package org.xue.app.service;

import org.xue.app.dto.UserSettingsDTO;
import org.xue.app.entity.User;

public interface UserSettingsService {
    UserSettingsDTO getUserSettings(String userId);
    User updateUserSettings(String userId, UserSettingsDTO userSettingsDTO);
}

package org.xue.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.xue.app.dto.UserSettingsDTO;
import org.xue.app.entity.User;
import org.xue.app.repository.UserRepository;
import org.xue.app.service.UserSettingsService;

import java.util.Optional;

@Service
public class UserSettingsServiceImpl implements UserSettingsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Assuming PasswordEncoder is available

    @Override
    public UserSettingsDTO getUserSettings(String userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            UserSettingsDTO dto = new UserSettingsDTO();
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            // Password is not sent to the client
            return dto;
        }
        return null; // Or throw an exception
    }

    @Override
    public User updateUserSettings(String userId, UserSettingsDTO userSettingsDTO) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Update username if provided and different
            if (userSettingsDTO.getUsername() != null && !userSettingsDTO.getUsername().equals(user.getUsername())) {
                user.setUsername(userSettingsDTO.getUsername());
            }
            // Update email if provided and different
            if (userSettingsDTO.getEmail() != null && !userSettingsDTO.getEmail().equals(user.getEmail())) {
                user.setEmail(userSettingsDTO.getEmail());
            }
            // Update password if provided
            if (userSettingsDTO.getPassword() != null && !userSettingsDTO.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userSettingsDTO.getPassword()));
            }
            return userRepository.save(user);
        }
        return null; // Or throw an exception
    }
}

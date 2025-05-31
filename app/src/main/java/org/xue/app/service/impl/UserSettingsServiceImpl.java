package org.xue.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xue.app.dto.UserSettingsDTO;
import org.xue.app.entity.User;
import org.xue.app.repository.UserRepository;
import org.xue.app.service.UserSettingsService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserSettingsServiceImpl implements UserSettingsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.upload.avatar-dir:./uploads/avatars/}")
    private String avatarUploadDir;

    @Override
    public UserSettingsDTO getUserSettings(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            UserSettingsDTO dto = new UserSettingsDTO();
            dto.setUsername(user.getUsername());
            dto.setNickname(user.getNickname());
            dto.setEmail(user.getEmail());
            dto.setAvatarUrl(user.getAvatarUrl());
            return dto;
        }
        return null;
    }

    @Override
    public User updateUserSettings(String username, UserSettingsDTO userSettingsDTO) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // Update nickname if provided
            if (userSettingsDTO.getNickname() != null && !userSettingsDTO.getNickname().trim().isEmpty()) {
                user.setNickname(userSettingsDTO.getNickname().trim());
            }
            
            // Update email if provided
            if (userSettingsDTO.getEmail() != null && !userSettingsDTO.getEmail().trim().isEmpty()) {
                user.setEmail(userSettingsDTO.getEmail().trim());
            }
            
            // Update password if provided and current password is verified
            if (userSettingsDTO.getPassword() != null && !userSettingsDTO.getPassword().isEmpty() &&
                userSettingsDTO.getCurrentPassword() != null && !userSettingsDTO.getCurrentPassword().isEmpty()) {
                
                if (passwordEncoder.matches(userSettingsDTO.getCurrentPassword(), user.getPassword())) {
                    user.setPassword(passwordEncoder.encode(userSettingsDTO.getPassword()));
                } else {
                    throw new RuntimeException("当前密码不正确");
                }
            }
            
            return userRepository.save(user);
        }
        throw new RuntimeException("用户不存在");
    }

    @Override
    public boolean updateNickname(String username, String nickname) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setNickname(nickname.trim());
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateEmail(String username, String email) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setEmail(email.trim());
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public boolean updatePassword(String username, String currentPassword, String newPassword) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // 验证当前密码
            if (passwordEncoder.matches(currentPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    @Override
    public String uploadAvatar(String username, MultipartFile file) {
        try {
            // 创建上传目录
            Path uploadPath = Paths.get(avatarUploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = username + "_" + UUID.randomUUID().toString() + fileExtension;

            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // 更新用户头像URL
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                String avatarUrl = "/uploads/avatars/" + filename;
                user.setAvatarUrl(avatarUrl);
                userRepository.save(user);
                return avatarUrl;
            }

        } catch (IOException e) {
            throw new RuntimeException("头像上传失败: " + e.getMessage());
        }
        
        throw new RuntimeException("用户不存在");
    }

    @Override
    public boolean verifyCurrentPassword(String username, String currentPassword) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return passwordEncoder.matches(currentPassword, user.getPassword());
        }
        return false;
    }
}

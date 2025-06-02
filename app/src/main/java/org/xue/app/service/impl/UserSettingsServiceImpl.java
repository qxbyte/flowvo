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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserSettingsServiceImpl implements UserSettingsService {

    private static final Logger logger = LoggerFactory.getLogger(UserSettingsServiceImpl.class);

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
            // 处理相对路径，转换为绝对路径
            String absoluteAvatarDir;
            if (avatarUploadDir.startsWith("./")) {
                // 获取当前工作目录（应该是项目根目录）
                String workingDir = System.getProperty("user.dir");
                logger.info("当前工作目录: {}", workingDir);
                
                // 如果工作目录是app子目录，需要返回到项目根目录
                if (workingDir.endsWith("/app") || workingDir.endsWith("\\app")) {
                    workingDir = new File(workingDir).getParent();
                    logger.info("调整工作目录到项目根目录: {}", workingDir);
                }
                
                absoluteAvatarDir = Paths.get(workingDir, avatarUploadDir.substring(2)).toString();
            } else {
                absoluteAvatarDir = avatarUploadDir;
            }
            
            logger.info("头像上传目录: {}", absoluteAvatarDir);
            
            // 创建上传目录
            Path uploadPath = Paths.get(absoluteAvatarDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.info("创建头像上传目录: {}", uploadPath);
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
            logger.info("头像文件保存到: {}", filePath);

            // 更新用户头像URL
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                String avatarUrl = "/uploads/avatars/" + filename;
                user.setAvatarUrl(avatarUrl);
                userRepository.save(user);
                logger.info("用户 {} 头像URL更新为: {}", username, avatarUrl);
                return avatarUrl;
            }

        } catch (IOException e) {
            logger.error("头像上传失败: {}", e.getMessage(), e);
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

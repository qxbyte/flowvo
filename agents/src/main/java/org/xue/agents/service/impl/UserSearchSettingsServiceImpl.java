package org.xue.agents.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xue.agents.entity.UserSearchSettings;
import org.xue.agents.repository.UserSearchSettingsRepository;
import org.xue.agents.service.UserSearchSettingsService;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户搜索设置服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserSearchSettingsServiceImpl implements UserSearchSettingsService {
    
    private final UserSearchSettingsRepository userSearchSettingsRepository;
    
    @Override
    public UserSearchSettings getUserSettings(String userId) {
        log.debug("获取用户搜索设置: userId={}", userId);
        
        Optional<UserSearchSettings> settingsOpt = userSearchSettingsRepository.findByUserId(userId);
        
        if (settingsOpt.isPresent()) {
            log.debug("找到用户设置: {}", settingsOpt.get());
            return settingsOpt.get();
        } else {
            log.debug("用户无设置，返回默认设置: userId={}", userId);
            return getDefaultSettings();
        }
    }
    
    @Override
    @Transactional
    public UserSearchSettings saveUserSettings(String userId, UserSearchSettings settings) {
        log.info("保存用户搜索设置: userId={}, settings={}", userId, settings);
        
        Optional<UserSearchSettings> existingOpt = userSearchSettingsRepository.findByUserId(userId);
        
        UserSearchSettings toSave;
        if (existingOpt.isPresent()) {
            // 更新现有设置
            toSave = existingOpt.get();
            toSave.setTopK(settings.getTopK());
            toSave.setSimilarityThreshold(settings.getSimilarityThreshold());
            toSave.setMaxTokens(settings.getMaxTokens());
            toSave.setTemperature(settings.getTemperature());
            toSave.setUpdatedAt(LocalDateTime.now());
            log.debug("更新现有用户设置");
        } else {
            // 创建新设置
            toSave = UserSearchSettings.builder()
                    .userId(userId)
                    .topK(settings.getTopK())
                    .similarityThreshold(settings.getSimilarityThreshold())
                    .maxTokens(settings.getMaxTokens())
                    .temperature(settings.getTemperature())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            log.debug("创建新用户设置");
        }
        
        // 参数验证
        validateSettings(toSave);
        
        UserSearchSettings saved = userSearchSettingsRepository.save(toSave);
        log.info("用户搜索设置保存成功: userId={}, id={}", userId, saved.getId());
        
        return saved;
    }
    
    @Override
    @Transactional
    public UserSearchSettings resetUserSettings(String userId) {
        log.info("重置用户搜索设置: userId={}", userId);
        
        // 删除现有设置
        userSearchSettingsRepository.deleteByUserId(userId);
        
        // 创建默认设置
        UserSearchSettings defaultSettings = UserSearchSettings.builder()
                .userId(userId)
                .topK(5)
                .similarityThreshold(0.7)
                .maxTokens(2000)
                .temperature(0.1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        UserSearchSettings saved = userSearchSettingsRepository.save(defaultSettings);
        log.info("用户搜索设置重置成功: userId={}", userId);
        
        return saved;
    }
    
    @Override
    @Transactional
    public void deleteUserSettings(String userId) {
        log.info("删除用户搜索设置: userId={}", userId);
        userSearchSettingsRepository.deleteByUserId(userId);
    }
    
    @Override
    public UserSearchSettings getDefaultSettings() {
        return UserSearchSettings.builder()
                .topK(5)
                .similarityThreshold(0.7)
                .maxTokens(2000)
                .temperature(0.1)
                .build();
    }
    
    /**
     * 验证设置参数
     */
    private void validateSettings(UserSearchSettings settings) {
        if (settings.getTopK() == null || settings.getTopK() < 1 || settings.getTopK() > 20) {
            throw new IllegalArgumentException("topK必须在1-20之间");
        }
        
        if (settings.getSimilarityThreshold() == null || 
            settings.getSimilarityThreshold() < 0.0 || settings.getSimilarityThreshold() > 1.0) {
            throw new IllegalArgumentException("相似度阈值必须在0.0-1.0之间");
        }
        
        if (settings.getMaxTokens() == null || settings.getMaxTokens() < 100 || settings.getMaxTokens() > 8000) {
            throw new IllegalArgumentException("最大令牌数必须在100-8000之间");
        }
        
        if (settings.getTemperature() == null || 
            settings.getTemperature() < 0.0 || settings.getTemperature() > 2.0) {
            throw new IllegalArgumentException("温度参数必须在0.0-2.0之间");
        }
    }
} 
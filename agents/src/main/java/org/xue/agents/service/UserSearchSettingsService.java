package org.xue.agents.service;

import org.xue.agents.entity.UserSearchSettings;

/**
 * 用户搜索设置服务接口
 */
public interface UserSearchSettingsService {
    
    /**
     * 获取用户搜索设置
     * @param userId 用户ID
     * @return 用户搜索设置，如果不存在则返回默认设置
     */
    UserSearchSettings getUserSettings(String userId);
    
    /**
     * 保存或更新用户搜索设置
     * @param userId 用户ID
     * @param settings 搜索设置
     * @return 保存后的设置
     */
    UserSearchSettings saveUserSettings(String userId, UserSearchSettings settings);
    
    /**
     * 重置用户搜索设置为默认值
     * @param userId 用户ID
     * @return 重置后的设置
     */
    UserSearchSettings resetUserSettings(String userId);
    
    /**
     * 删除用户搜索设置
     * @param userId 用户ID
     */
    void deleteUserSettings(String userId);
    
    /**
     * 获取默认搜索设置
     * @return 默认设置
     */
    UserSearchSettings getDefaultSettings();
} 
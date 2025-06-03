package org.xue.agents.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.xue.agents.entity.UserSearchSettings;

import java.util.Optional;

/**
 * 用户搜索设置Repository
 */
@Repository
public interface UserSearchSettingsRepository extends JpaRepository<UserSearchSettings, Long> {
    
    /**
     * 根据用户ID查找设置
     */
    Optional<UserSearchSettings> findByUserId(String userId);
    
    /**
     * 检查用户是否已有设置
     */
    boolean existsByUserId(String userId);
    
    /**
     * 根据用户ID删除设置
     */
    void deleteByUserId(String userId);
} 
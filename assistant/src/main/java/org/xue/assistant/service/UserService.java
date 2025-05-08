package org.xue.assistant.service;

import org.xue.assistant.entity.User;

import java.util.Optional;

public interface UserService {
    // 用户登录验证
    boolean validateUser(String username, String password);
    
    // 根据用户名查找用户
    Optional<User> findByUsername(String username);
    
    // 根据用户名直接获取用户，不存在时抛出异常
    User getUserByUsername(String username);
    
    // 保存用户
    User saveUser(User user);
    
    // 根据ID查找用户
    Optional<User> findById(Long id);
    
    // 删除用户
    void deleteUser(Long id);
}

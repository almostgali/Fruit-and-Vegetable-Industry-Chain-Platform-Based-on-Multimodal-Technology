package org.back.service;

import org.back.entity.User;

public interface UserService {
    // 用户登录
    User login(String username, String password);

    // 用户注册
    boolean register(User user);

    // 检查用户名是否已存在
    boolean checkUsernameExists(String username);

    // 根据ID查询用户
    User findById(Integer id);

    // 更新用户信息
    boolean update(User user);
}
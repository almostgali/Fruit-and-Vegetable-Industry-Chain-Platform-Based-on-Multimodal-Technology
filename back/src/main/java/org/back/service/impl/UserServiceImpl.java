package org.back.service.impl;

import org.back.entity.User;
import org.back.mapper.UserMapper;
import org.back.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Date;@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;  @Override
    public User login(String username, String password) {
        // 查询用户
        User user = userMapper.findByUsername(username);
        if (user != null) {
            // 对密码进行MD5加密后比较
            String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
            if (md5Password.equals(user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    @Override
    public boolean register(User user) {
        // 检查用户名是否已存在
        if (checkUsernameExists(user.getUsername())) {
            return false;
        }

        // 对密码进行MD5加密
        String md5Password = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
        user.setPassword(md5Password);

        // 设置创建和更新时间
        Date now = new Date();
        user.setCreateTime(now);
        user.setUpdateTime(now);

        // 插入用户
        return userMapper.register(user) > 0;
    }

    @Override
    public boolean checkUsernameExists(String username) {
        User user = userMapper.findByUsername(username);
        return user != null;
    }

    @Override
    public User findById(Integer id) {
        return userMapper.findById(id);
    }

    @Override
    public boolean update(User user) {
        // 设置更新时间
        user.setUpdateTime(new Date());

        // 如果修改了密码，则进行MD5加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            String md5Password = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
            user.setPassword(md5Password);
        }

        return userMapper.update(user) > 0;
    }
}
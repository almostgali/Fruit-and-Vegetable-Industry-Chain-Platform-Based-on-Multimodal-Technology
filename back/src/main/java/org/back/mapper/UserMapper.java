package org.back.mapper;

import org.back.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    // 根据用户名查询用户
    User findByUsername(String username);

    // 注册用户
    int register(User user);

    // 更新用户信息
    int update(User user);

    // 根据ID查询用户
    User findById(Integer id);
}
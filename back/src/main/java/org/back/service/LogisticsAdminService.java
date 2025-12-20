package org.back.service;

import org.back.entity.LogisticsAdmin;

/**
 * 物流管理员服务接口
 */
public interface LogisticsAdminService {
    
    /**
     * 管理员登录
     */
    LogisticsAdmin login(String username, String password);
    
    /**
     * 根据用户名查找管理员
     */
    LogisticsAdmin findByUsername(String username);
    
    /**
     * 创建管理员
     */
    boolean createAdmin(LogisticsAdmin admin);
    
    /**
     * 更新管理员信息
     */
    boolean updateAdmin(LogisticsAdmin admin);
}
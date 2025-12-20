package org.back.service.impl;

import org.back.entity.LogisticsAdmin;
import org.back.mapper.LogisticsAdminMapper;
import org.back.service.LogisticsAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

/**
 * 物流管理员服务实现类
 */
@Service
public class LogisticsAdminServiceImpl implements LogisticsAdminService {
    
    @Autowired
    private LogisticsAdminMapper logisticsAdminMapper;
    
    @Override
    public LogisticsAdmin login(String username, String password) {
        // 对密码进行MD5加密
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        return logisticsAdminMapper.findByUsernameAndPassword(username, encryptedPassword);
    }
    
    @Override
    public LogisticsAdmin findByUsername(String username) {
        return logisticsAdminMapper.findByUsername(username);
    }
    
    @Override
    public boolean createAdmin(LogisticsAdmin admin) {
        try {
            // 对密码进行MD5加密
            admin.setPassword(DigestUtils.md5DigestAsHex(admin.getPassword().getBytes()));
            admin.setCreateTime(LocalDateTime.now());
            admin.setUpdateTime(LocalDateTime.now());
            admin.setStatus(1); // 默认启用
            
            return logisticsAdminMapper.insert(admin) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean updateAdmin(LogisticsAdmin admin) {
        try {
            admin.setUpdateTime(LocalDateTime.now());
            return logisticsAdminMapper.updateById(admin) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
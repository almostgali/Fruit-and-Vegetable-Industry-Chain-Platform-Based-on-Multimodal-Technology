package org.back.service.impl;

import org.back.entity.LogisticsTracking;
import org.back.mapper.LogisticsTrackingMapper;
import org.back.service.LogisticsTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物流追踪服务实现类
 */
@Service
public class LogisticsTrackingServiceImpl implements LogisticsTrackingService {
    
    @Autowired
    private LogisticsTrackingMapper logisticsTrackingMapper;
    
    @Override
    public boolean createTracking(LogisticsTracking logisticsTracking) {
        try {
            logisticsTracking.setCreateTime(LocalDateTime.now());
            logisticsTracking.setUpdateTime(LocalDateTime.now());
            return logisticsTrackingMapper.insert(logisticsTracking) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public LogisticsTracking getByTrackingNumber(String trackingNumber) {
        try {
            return logisticsTrackingMapper.selectByTrackingNumber(trackingNumber);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public List<LogisticsTracking> getAllTrackings() {
        try {
            return logisticsTrackingMapper.selectAll();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public List<LogisticsTracking> getByStatus(String status) {
        try {
            return logisticsTrackingMapper.selectByStatus(status);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public boolean updateTracking(LogisticsTracking logisticsTracking) {
        try {
            logisticsTracking.setUpdateTime(LocalDateTime.now());
            return logisticsTrackingMapper.updateById(logisticsTracking) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean deleteTracking(Long id) {
        try {
            return logisticsTrackingMapper.deleteById(id) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
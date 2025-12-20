package org.back.service;

import org.back.entity.LogisticsTracking;

import java.util.List;

/**
 * 物流追踪服务接口
 */
public interface LogisticsTrackingService {
    
    /**
     * 创建物流追踪记录
     */
    boolean createTracking(LogisticsTracking logisticsTracking);
    
    /**
     * 根据追踪号查询
     */
    LogisticsTracking getByTrackingNumber(String trackingNumber);
    
    /**
     * 查询所有物流记录
     */
    List<LogisticsTracking> getAllTrackings();
    
    /**
     * 根据状态查询
     */
    List<LogisticsTracking> getByStatus(String status);
    
    /**
     * 更新物流状态
     */
    boolean updateTracking(LogisticsTracking logisticsTracking);
    
    /**
     * 删除物流记录
     */
    boolean deleteTracking(Long id);
}
package org.back.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.back.entity.LogisticsTracking;

import java.util.List;

/**
 * 物流追踪Mapper接口
 */
@Mapper
public interface LogisticsTrackingMapper {
    
    /**
     * 插入物流追踪记录
     */
    int insert(LogisticsTracking logisticsTracking);
    
    /**
     * 根据ID查询
     */
    LogisticsTracking selectById(@Param("id") Long id);
    
    /**
     * 根据追踪号查询
     */
    LogisticsTracking selectByTrackingNumber(@Param("trackingNumber") String trackingNumber);
    
    /**
     * 查询所有记录
     */
    List<LogisticsTracking> selectAll();
    
    /**
     * 根据状态查询
     */
    List<LogisticsTracking> selectByStatus(@Param("status") String status);
    
    /**
     * 更新物流信息
     */
    int updateById(LogisticsTracking logisticsTracking);
    
    /**
     * 删除记录
     */
    int deleteById(@Param("id") Long id);
}
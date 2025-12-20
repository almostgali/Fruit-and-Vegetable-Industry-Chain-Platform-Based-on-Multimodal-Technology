package org.back.service;

import org.back.entity.LogisticsOrder;
import org.back.entity.LogisticsStatistics;

import java.util.List;
import java.util.Map;

/**
 * 物流管理服务接口
 */
public interface LogisticsManagementService {
    
    /**
     * 获取当前包裹数量
     */
    Integer getCurrentPackageCount();
    
    /**
     * 获取包裹类型占比
     */
    Map<String, Object> getPackageTypeRatio();
    
    /**
     * 获取各省数据
     */
    List<Map<String, Object>> getProvinceData();
    
    /**
     * 获取城市排行
     */
    List<Map<String, Object>> getCityRanking();
    
    /**
     * 获取时间序列数据
     */
    List<Map<String, Object>> getTimeSeriesData();
    
    /**
     * 创建订单
     */
    LogisticsOrder createOrder(LogisticsOrder order);
    
    /**
     * 更新订单状态
     */
    void updateOrderStatus(Long orderId, Integer status);
    
    /**
     * 批量创建订单
     */
    int batchCreateOrders(List<LogisticsOrder> orders);
    
    /**
     * 获取订单列表
     */
    Map<String, Object> getOrderList(Integer page, Integer size, String city, Integer status);
    
    /**
     * 生成测试数据
     */
    int generateTestData(Integer count);
    
    /**
     * 获取统计数据
     */
    List<LogisticsStatistics> getStatistics(String startDate, String endDate, String province, String city);
    
    /**
     * 获取大屏数据
     */
    Map<String, Object> getDashboardData();

    /**
     * 获取统计概要（总数、运输中、完成、异常）
     */
    Map<String, Object> getStatisticsSummary();
}
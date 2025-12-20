package org.back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.back.entity.LogisticsOrder;

import java.util.List;
import java.util.Map;

/**
 * 物流订单Mapper接口
 */
@Mapper
public interface LogisticsOrderMapper extends BaseMapper<LogisticsOrder> {
    
    /**
     * 获取各省统计数据
     */
    @Select("SELECT " +
            "receiver_province as province, " +
            "COUNT(*) as totalCount, " +
            "SUM(CASE WHEN order_status = 5 THEN 1 ELSE 0 END) as deliveredCount, " +
            "SUM(CASE WHEN order_status IN (2,3,4) THEN 1 ELSE 0 END) as inTransitCount " +
            "FROM logistics_order " +
            "GROUP BY receiver_province " +
            "ORDER BY totalCount DESC")
    List<Map<String, Object>> getProvinceStatistics();
    
    /**
     * 获取城市排行
     */
    @Select("SELECT " +
            "receiver_city as city, " +
            "receiver_province as province, " +
            "COUNT(*) as packageCount " +
            "FROM logistics_order " +
            "WHERE order_status IN (2,3,4,5) " +
            "GROUP BY receiver_city, receiver_province " +
            "ORDER BY packageCount DESC " +
            "LIMIT 10")
    List<Map<String, Object>> getCityRanking();
    
    /**
     * 获取时间序列数据（最近7天）
     */
    @Select("SELECT " +
            "DATE(created_time) as date, " +
            "COUNT(*) as orderCount, " +
            "SUM(CASE WHEN package_type = 0 THEN 1 ELSE 0 END) as fileCount, " +
            "SUM(CASE WHEN package_type = 1 THEN 1 ELSE 0 END) as itemCount " +
            "FROM logistics_order " +
            "WHERE created_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY DATE(created_time) " +
            "ORDER BY date ASC")
    List<Map<String, Object>> getTimeSeriesData();
    
    /**
     * 分页查询所有订单列表
     */
    @Select("SELECT id,order_no,tracking_no,company_id,sender_name,sender_phone,sender_address," +
            "sender_city,sender_province,receiver_name,receiver_phone,receiver_address," +
            "receiver_city,receiver_province,package_type,package_weight,package_value," +
            "freight_cost,order_status,created_time,updated_time " +
            "FROM logistics_order " +
            "ORDER BY created_time DESC")
    IPage<LogisticsOrder> selectAllOrderPage(IPage<LogisticsOrder> page);
    
    /**
     * 根据城市分页查询订单列表
     */
    @Select("SELECT id,order_no,tracking_no,company_id,sender_name,sender_phone,sender_address," +
            "sender_city,sender_province,receiver_name,receiver_phone,receiver_address," +
            "receiver_city,receiver_province,package_type,package_weight,package_value," +
            "freight_cost,order_status,created_time,updated_time " +
            "FROM logistics_order " +
            "WHERE receiver_city LIKE CONCAT('%', #{city}, '%') OR sender_city LIKE CONCAT('%', #{city}, '%') " +
            "ORDER BY created_time DESC")
    IPage<LogisticsOrder> selectOrderPageByCity(IPage<LogisticsOrder> page, @Param("city") String city);
    
    /**
     * 根据状态分页查询订单列表
     */
    @Select("SELECT id,order_no,tracking_no,company_id,sender_name,sender_phone,sender_address," +
            "sender_city,sender_province,receiver_name,receiver_phone,receiver_address," +
            "receiver_city,receiver_province,package_type,package_weight,package_value," +
            "freight_cost,order_status,created_time,updated_time " +
            "FROM logistics_order " +
            "WHERE order_status = #{status} " +
            "ORDER BY created_time DESC")
    IPage<LogisticsOrder> selectOrderPageByStatus(IPage<LogisticsOrder> page, @Param("status") Integer status);
    
    /**
     * 根据城市和状态分页查询订单列表
     */
    @Select("SELECT id,order_no,tracking_no,company_id,sender_name,sender_phone,sender_address," +
            "sender_city,sender_province,receiver_name,receiver_phone,receiver_address," +
            "receiver_city,receiver_province,package_type,package_weight,package_value," +
            "freight_cost,order_status,created_time,updated_time " +
            "FROM logistics_order " +
            "WHERE (receiver_city LIKE CONCAT('%', #{city}, '%') OR sender_city LIKE CONCAT('%', #{city}, '%')) " +
            "AND order_status = #{status} " +
            "ORDER BY created_time DESC")
    IPage<LogisticsOrder> selectOrderPageByCityAndStatus(IPage<LogisticsOrder> page, @Param("city") String city, @Param("status") Integer status);
    
    /**
     * 简单查询所有订单（用于测试）
     */
    @Select("SELECT id,order_no,tracking_no,company_id,sender_name,sender_phone,sender_address," +
            "sender_city,sender_province,receiver_name,receiver_phone,receiver_address," +
            "receiver_city,receiver_province,package_type,package_weight,package_value," +
            "freight_cost,order_status,created_time,updated_time " +
            "FROM logistics_order " +
            "ORDER BY created_time DESC " +
            "LIMIT 10")
    List<LogisticsOrder> selectSimpleOrderList();
}
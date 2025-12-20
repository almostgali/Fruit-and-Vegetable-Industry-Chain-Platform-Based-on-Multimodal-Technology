package org.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 物流统计数据实体类
 */
@Data
@TableName("logistics_statistics")
public class LogisticsStatistics {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private LocalDate statDate;
    private String province;
    private String city;
    private Long companyId;
    private Integer packageType; // 0-文件，1-物品
    private Integer totalOrders;
    private Integer deliveredOrders;
    private Integer inTransitOrders;
    private Integer warehouseInCount;
    private Integer warehouseOutCount;
    private BigDecimal totalWeight;
    private BigDecimal totalValue;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    
    // 关联字段（不存在于数据库表中）
    @TableField(exist = false)
    private String companyName;
}
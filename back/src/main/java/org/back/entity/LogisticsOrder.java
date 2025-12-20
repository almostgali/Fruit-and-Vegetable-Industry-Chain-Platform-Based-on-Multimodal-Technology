package org.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物流订单实体类
 */
@Data
@TableName("logistics_order")
public class LogisticsOrder {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String orderNo;
    private String trackingNo;
    private Long companyId;
    private String senderName;
    private String senderPhone;
    private String senderAddress;
    private String senderCity;
    private String senderProvince;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String receiverCity;
    private String receiverProvince;
    private Integer packageType; // 0-文件，1-物品
    private BigDecimal packageWeight;
    private BigDecimal packageValue;
    private BigDecimal freightCost;
    private Integer orderStatus; // 1-已下单，2-已揽收，3-运输中，4-派送中，5-已签收，6-异常
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    
    // 关联字段（非数据库字段）
    @TableField(exist = false)
    private String companyName;
    @TableField(exist = false)
    private String statusDesc;
    
    public String getStatusDesc() {
        if (orderStatus == null) return "未知";
        switch (orderStatus) {
            case 1: return "已下单";
            case 2: return "已揽收";
            case 3: return "运输中";
            case 4: return "派送中";
            case 5: return "已签收";
            case 6: return "异常";
            default: return "未知";
        }
    }
    
    public String getPackageTypeDesc() {
        if (packageType == null) return "未知";
        return packageType == 0 ? "文件" : "物品";
    }
}
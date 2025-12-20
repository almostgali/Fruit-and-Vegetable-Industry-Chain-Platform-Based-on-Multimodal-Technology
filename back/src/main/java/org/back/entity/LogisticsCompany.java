package org.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物流公司实体类
 */
@Data
@TableName("logistics_company")
public class LogisticsCompany {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String companyName;
    private String companyCode;
    private String contactPhone;
    private String address;
    private Integer status; // 1-正常，0-停用
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
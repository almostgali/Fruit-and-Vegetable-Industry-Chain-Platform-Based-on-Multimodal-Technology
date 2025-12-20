package org.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物流管理员实体类
 */
@Data
@TableName("logistics_admin")
public class LogisticsAdmin {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    private String password;
    
    private String realName;
    
    private String email;
    
    private String phone;
    
    private Integer status; // 1-启用 0-禁用
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
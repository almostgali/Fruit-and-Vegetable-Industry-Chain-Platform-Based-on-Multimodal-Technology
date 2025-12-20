package org.back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.back.entity.LogisticsAdmin;

/**
 * 物流管理员Mapper接口
 */
@Mapper
public interface LogisticsAdminMapper extends BaseMapper<LogisticsAdmin> {
    
    /**
     * 根据用户名和密码查找管理员
     */
    @Select("SELECT * FROM logistics_admin WHERE username = #{username} AND password = #{password} AND status = 1")
    LogisticsAdmin findByUsernameAndPassword(@Param("username") String username, @Param("password") String password);
    
    /**
     * 根据用户名查找管理员
     */
    @Select("SELECT * FROM logistics_admin WHERE username = #{username}")
    LogisticsAdmin findByUsername(@Param("username") String username);
}
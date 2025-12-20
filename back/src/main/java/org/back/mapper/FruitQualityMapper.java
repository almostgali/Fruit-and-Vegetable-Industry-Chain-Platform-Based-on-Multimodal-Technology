package org.back.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.back.entity.FruitQuality;

import java.util.List;

/**
 * 水果品质检测结果Mapper接口
 */
@Mapper
public interface FruitQualityMapper {
    
    /**
     * 插入检测结果
     */
    int insert(FruitQuality fruitQuality);
    
    /**
     * 根据ID查询
     */
    FruitQuality selectById(@Param("id") Long id);
    
    /**
     * 根据用户ID查询检测历史
     */
    List<FruitQuality> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 查询所有检测结果
     */
    List<FruitQuality> selectAll();
    
    /**
     * 更新检测结果
     */
    int updateById(FruitQuality fruitQuality);
    
    /**
     * 删除检测结果
     */
    int deleteById(@Param("id") Long id);
}
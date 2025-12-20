package org.back.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.back.entity.FruitMaturity;

import java.util.List;

/**
 * 水果新鲜度检测结果Mapper接口
 */
@Mapper
public interface FruitMaturityMapper {
    
    /**
     * 插入检测结果
     */
    int insert(FruitMaturity fruitMaturity);
    
    /**
     * 根据ID查询
     */
    FruitMaturity selectById(@Param("id") Long id);
    
    /**
     * 根据用户ID查询检测历史
     */
    List<FruitMaturity> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 查询所有检测结果
     */
    List<FruitMaturity> selectAll();
    
    /**
     * 更新检测结果
     */
    int updateById(FruitMaturity fruitMaturity);
    
    /**
     * 删除检测结果
     */
    int deleteById(@Param("id") Long id);
}
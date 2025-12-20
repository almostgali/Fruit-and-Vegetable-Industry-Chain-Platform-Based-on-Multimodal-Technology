package org.back.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 模型服务接口
 */
public interface ModelService {
    
    /**
     * 水果品质检测
     * @param imageFile 图片文件
     * @return 检测结果
     */
    Map<String, Object> detectFruitQuality(MultipartFile imageFile);
    
    /**
     * 水果新鲜度检测
     * @param imageFile 图片文件
     * @return 检测结果
     */
    Map<String, Object> detectFruitMaturity(MultipartFile imageFile);
    
    /**
     * 保存检测结果
     * @param userId 用户ID
     * @param imageUrl 图片URL
     * @param resultMap 检测结果
     * @param type 检测类型（quality/maturity）
     * @return 是否保存成功
     */
    boolean saveDetectionResult(Long userId, String imageUrl, Map<String, Object> resultMap, String type);
}
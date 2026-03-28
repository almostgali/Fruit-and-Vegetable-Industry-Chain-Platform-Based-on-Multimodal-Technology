package org.back.controller;

import org.back.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 模型检测控制器
 */
@RestController
@RequestMapping("/api/model")
public class ModelController {

    @Autowired
    private ModelService modelService;

    @PostMapping("/detect/quality")
    public Map<String, Object> detectQuality(@RequestParam("file") MultipartFile file, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("message", "请选择要检测的文件");
                return result;
            }

            // 获取当前登录用户ID
            Object userObj = session.getAttribute("user");
            Long userId = null;
            if (userObj != null) {
                try {
                    Integer userIdInt = ((org.back.entity.User) userObj).getId();
                    userId = userIdInt != null ? userIdInt.longValue() : 1L;
                } catch (Exception e) {
                    userId = 1L; // 默认用户ID
                }
            } else {
                userId = 1L; // 默认用户ID
            }

            Map<String, Object> detectionResult = modelService.detectFruitQuality(file);

            if (detectionResult != null && detectionResult.containsKey("error")) {
                result.put("success", false);
                result.put("message", String.valueOf(detectionResult.get("error")));
                result.put("data", detectionResult);
                return result;
            }
            
            // 保存检测结果到数据库
            if (detectionResult != null && !detectionResult.containsKey("error")) {
                String imageUrl = (String) detectionResult.getOrDefault("image_url", file.getOriginalFilename());
                modelService.saveDetectionResult(userId, imageUrl, detectionResult, "quality");
            }
            
            result.put("success", true);
            result.put("data", detectionResult);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "检测失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/detect/maturity")
    public Map<String, Object> detectMaturity(@RequestParam("file") MultipartFile file, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("message", "请选择要检测的文件");
                return result;
            }

            // 获取当前登录用户ID
            Object userObj = session.getAttribute("user");
            Long userId = null;
            if (userObj != null) {
                try {
                    Integer userIdInt = ((org.back.entity.User) userObj).getId();
                    userId = userIdInt != null ? userIdInt.longValue() : 1L;
                } catch (Exception e) {
                    userId = 1L; // 默认用户ID
                }
            } else {
                userId = 1L; // 默认用户ID
            }

            Map<String, Object> detectionResult = modelService.detectFruitMaturity(file);

            if (detectionResult != null && detectionResult.containsKey("error")) {
                result.put("success", false);
                result.put("message", String.valueOf(detectionResult.get("error")));
                result.put("data", detectionResult);
                return result;
            }
            
            // 保存检测结果到数据库
            if (detectionResult != null && !detectionResult.containsKey("error")) {
                String imageUrl = (String) detectionResult.getOrDefault("image_url", file.getOriginalFilename());
                modelService.saveDetectionResult(userId, imageUrl, detectionResult, "maturity");
            }
            
            result.put("success", true);
            result.put("data", detectionResult);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "检测失败：" + e.getMessage());
        }
        return result;
    }
}

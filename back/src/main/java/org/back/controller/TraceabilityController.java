package org.back.controller;

import org.back.entity.LogisticsTracking;
import org.back.service.LogisticsTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/traceability")
public class TraceabilityController {

    @Autowired
    private LogisticsTrackingService logisticsTrackingService;

    @GetMapping("")
    public String traceability(Model model) {
        List<LogisticsTracking> trackings = logisticsTrackingService.getAllTrackings();
        model.addAttribute("trackings", trackings);
        return "traceability";
    }

    @PostMapping("/track")
    @ResponseBody
    public Map<String, Object> trackByNumber(@RequestParam String trackingNumber) {
        Map<String, Object> result = new HashMap<>();
        try {
            LogisticsTracking tracking = logisticsTrackingService.getByTrackingNumber(trackingNumber);
            if (tracking != null) {
                result.put("success", true);
                result.put("data", tracking);
            } else {
                result.put("success", false);
                result.put("message", "未找到该追踪号的物流信息");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/create")
    @ResponseBody
    public Map<String, Object> createTracking(@RequestBody LogisticsTracking tracking) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = logisticsTrackingService.createTracking(tracking);
            if (success) {
                result.put("success", true);
                result.put("message", "物流信息创建成功");
            } else {
                result.put("success", false);
                result.put("message", "物流信息创建失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "创建失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/update")
    @ResponseBody
    public Map<String, Object> updateTracking(@RequestBody LogisticsTracking tracking) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = logisticsTrackingService.updateTracking(tracking);
            if (success) {
                result.put("success", true);
                result.put("message", "物流信息更新成功");
            } else {
                result.put("success", false);
                result.put("message", "物流信息更新失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
        }
        return result;
    }
}
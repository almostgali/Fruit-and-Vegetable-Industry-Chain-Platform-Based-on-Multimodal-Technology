package org.back.controller;

import org.back.entity.LogisticsOrder;
import org.back.entity.LogisticsStatistics;
import org.back.service.LogisticsManagementService;
import org.back.websocket.LogisticsDashboardWebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 物流管理控制器
 */
@RestController
@RequestMapping("/api/logistics")
@CrossOrigin(origins = "*")
public class LogisticsManagementController {

    @Autowired
    private LogisticsManagementService logisticsService;

    /**
     * 获取大屏实时数据
     */
    @GetMapping("/dashboard/realtime")
    public Map<String, Object> getDashboardData() {
        Map<String, Object> result = new HashMap<>();
        
        // 当前到件量
        Integer currentPackages = logisticsService.getCurrentPackageCount();
        result.put("currentPackages", currentPackages);
        
        // 派件入库量占比
        Map<String, Object> packageTypeRatio = logisticsService.getPackageTypeRatio();
        result.put("packageTypeRatio", packageTypeRatio);
        
        // 各省市数据
        List<Map<String, Object>> provinceData = logisticsService.getProvinceData();
        result.put("provinceData", provinceData);
        
        // 城市排行
        List<Map<String, Object>> cityRanking = logisticsService.getCityRanking();
        result.put("cityRanking", cityRanking);
        
        // 时间序列数据
        List<Map<String, Object>> timeSeriesData = logisticsService.getTimeSeriesData();
        result.put("timeSeriesData", timeSeriesData);
        
        return result;
    }

    /**
     * 创建物流订单
     */
    @PostMapping("/order")
    public Map<String, Object> createOrder(@RequestBody LogisticsOrder order) {
        Map<String, Object> result = new HashMap<>();
        try {
            LogisticsOrder createdOrder = logisticsService.createOrder(order);
            result.put("success", true);
            result.put("data", createdOrder);
            result.put("message", "订单创建成功");

            // 推送统计与大屏更新
            try {
                Map<String, Object> summary = logisticsService.getStatisticsSummary();
                LogisticsDashboardWebSocket.pushStatisticsUpdate(summary);
            } catch (Exception ignored) {}
            pushRealTimeUpdate();
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "订单创建失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/order/{orderId}/status")
    public Map<String, Object> updateOrderStatus(@PathVariable Long orderId, @RequestParam Integer status) {
        Map<String, Object> result = new HashMap<>();
        try {
            logisticsService.updateOrderStatus(orderId, status);
            result.put("success", true);
            result.put("message", "状态更新成功");

            // 推送统计与大屏更新
            try {
                Map<String, Object> summary = logisticsService.getStatisticsSummary();
                LogisticsDashboardWebSocket.pushStatisticsUpdate(summary);
            } catch (Exception ignored) {}
            pushRealTimeUpdate();
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "状态更新失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 批量录入订单数据
     */
    @PostMapping("/order/batch")
    public Map<String, Object> batchCreateOrders(@RequestBody List<LogisticsOrder> orders) {
        Map<String, Object> result = new HashMap<>();
        try {
            int successCount = logisticsService.batchCreateOrders(orders);
            result.put("success", true);
            result.put("successCount", successCount);
            result.put("message", "批量创建成功，共创建" + successCount + "条订单");

            // 推送统计与大屏更新
            try {
                Map<String, Object> summary = logisticsService.getStatisticsSummary();
                LogisticsDashboardWebSocket.pushStatisticsUpdate(summary);
            } catch (Exception ignored) {}
            pushRealTimeUpdate();
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "批量创建失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 测试接口
     */
    @GetMapping("/test/simple")
    public Map<String, Object> testSimple() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "测试成功");
        result.put("data", "Hello World");
        return result;
    }

    /**
     * 获取订单列表
     */
    @GetMapping("/order/list")
    public Map<String, Object> getOrderList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer status) {
        
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> pageData = logisticsService.getOrderList(page, size, city, status);
            result.put("success", true);
            result.put("data", pageData);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败：" + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
            e.printStackTrace(); // 打印完整的堆栈跟踪
        }
        return result;
    }

    /**
     * 生成测试数据
     */
    @PostMapping("/test/generate")
    public Map<String, Object> generateTestData(@RequestParam(defaultValue = "100") Integer count) {
        Map<String, Object> result = new HashMap<>();
        try {
            int generatedCount = logisticsService.generateTestData(count);
            result.put("success", true);
            result.put("generatedCount", generatedCount);
            result.put("message", "测试数据生成成功，共生成" + generatedCount + "条数据");

            // 推送统计与大屏更新
            try {
                Map<String, Object> summary = logisticsService.getStatisticsSummary();
                LogisticsDashboardWebSocket.pushStatisticsUpdate(summary);
            } catch (Exception ignored) {}
            pushRealTimeUpdate();
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "测试数据生成失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取统计数据
     */
    @GetMapping("/statistics")
    public Map<String, Object> getStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city) {
        
        Map<String, Object> result = new HashMap<>();
        try {
            List<LogisticsStatistics> statistics = logisticsService.getStatistics(startDate, endDate, province, city);
            result.put("success", true);
            result.put("data", statistics);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "统计数据查询失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取大屏数据（完整版本）
     */
    @GetMapping("/dashboard-data")
    public Map<String, Object> getFullDashboardData() {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> dashboardData = logisticsService.getDashboardData();
            result.put("success", true);
            result.put("data", dashboardData);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取大屏数据失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取统计概要（总数、运输中、完成、异常）
     */
    @GetMapping("/statistics/summary")
    public Map<String, Object> getStatisticsSummary() {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> summary = logisticsService.getStatisticsSummary();
            result.put("success", true);
            result.put("data", summary);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取统计概要失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 刷新大屏数据
     */
    @PostMapping("/refresh-dashboard")
    public Map<String, Object> refreshDashboard() {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> dashboardData = logisticsService.getDashboardData();
            
            // 通过WebSocket推送更新
            LogisticsDashboardWebSocket.pushDashboardUpdate(dashboardData);
            
            result.put("success", true);
            result.put("message", "大屏数据刷新成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "刷新大屏数据失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 推送实时数据更新（内部方法，在数据变更时调用）
     */
    private void pushRealTimeUpdate() {
        try {
            Map<String, Object> dashboardData = logisticsService.getDashboardData();
            LogisticsDashboardWebSocket.pushDashboardUpdate(dashboardData);
        } catch (Exception e) {
            // 记录日志但不影响主要业务流程
            System.err.println("推送实时数据失败: " + e.getMessage());
        }
    }
}
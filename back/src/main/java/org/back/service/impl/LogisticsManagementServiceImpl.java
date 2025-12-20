package org.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.back.entity.LogisticsOrder;
import org.back.entity.LogisticsStatistics;
import org.back.mapper.LogisticsOrderMapper;
import org.back.mapper.LogisticsStatisticsMapper;
import org.back.service.LogisticsManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 物流管理服务实现类
 */
@Service
public class LogisticsManagementServiceImpl implements LogisticsManagementService {

    @Autowired
    private LogisticsOrderMapper logisticsOrderMapper;
    
    @Autowired
    private LogisticsStatisticsMapper statisticsMapper;

    @Override
    public Integer getCurrentPackageCount() {
        QueryWrapper<LogisticsOrder> wrapper = new QueryWrapper<>();
        wrapper.in("order_status", Arrays.asList(2, 3, 4)); // 已揽收、运输中、派送中
        return Math.toIntExact(logisticsOrderMapper.selectCount(wrapper));
    }

    @Override
    public Map<String, Object> getPackageTypeRatio() {
        Map<String, Object> result = new HashMap<>();
        
        // 查询文件类型数量
        QueryWrapper<LogisticsOrder> fileWrapper = new QueryWrapper<>();
        fileWrapper.eq("package_type", 0);
        Integer fileCount = Math.toIntExact(logisticsOrderMapper.selectCount(fileWrapper));
        
        // 查询物品类型数量
        QueryWrapper<LogisticsOrder> itemWrapper = new QueryWrapper<>();
        itemWrapper.eq("package_type", 1);
        Integer itemCount = Math.toIntExact(logisticsOrderMapper.selectCount(itemWrapper));
        
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> fileData = new HashMap<>();
        fileData.put("name", "文件");
        fileData.put("value", fileCount);
        data.add(fileData);
        
        Map<String, Object> itemData = new HashMap<>();
        itemData.put("name", "物品");
        itemData.put("value", itemCount);
        data.add(itemData);
        
        result.put("data", data);
        result.put("total", fileCount + itemCount);
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getProvinceData() {
        return logisticsOrderMapper.getProvinceStatistics();
    }

    @Override
    public List<Map<String, Object>> getCityRanking() {
        return logisticsOrderMapper.getCityRanking();
    }

    @Override
    public List<Map<String, Object>> getTimeSeriesData() {
        return logisticsOrderMapper.getTimeSeriesData();
    }

    @Override
    public LogisticsOrder createOrder(LogisticsOrder order) {
        // 生成订单号和运单号
        order.setOrderNo(generateOrderNo());
        order.setTrackingNo(generateTrackingNo());
        order.setCreatedTime(LocalDateTime.now());
        order.setUpdatedTime(LocalDateTime.now());
        
        if (order.getOrderStatus() == null) {
            order.setOrderStatus(1); // 默认已下单状态
        }
        
        logisticsOrderMapper.insert(order);
        return order;
    }

    @Override
    public void updateOrderStatus(Long orderId, Integer status) {
        LogisticsOrder order = new LogisticsOrder();
        order.setId(orderId);
        order.setOrderStatus(status);
        order.setUpdatedTime(LocalDateTime.now());
        logisticsOrderMapper.updateById(order);
    }

    @Override
    public int batchCreateOrders(List<LogisticsOrder> orders) {
        int successCount = 0;
        for (LogisticsOrder order : orders) {
            try {
                createOrder(order);
                successCount++;
            } catch (Exception e) {
                // 记录错误但继续处理其他订单
                e.printStackTrace();
            }
        }
        return successCount;
    }

    @Override
    public Map<String, Object> getOrderList(Integer page, Integer size, String city, Integer status) {
        Page<LogisticsOrder> pageObj = new Page<>(page, size);
        
        IPage<LogisticsOrder> pageResult;
        
        // 根据参数选择不同的查询方法
        if (city != null && !city.trim().isEmpty() && status != null) {
            // 同时有城市和状态条件
            System.out.println("调用selectOrderPageByCityAndStatus，城市：" + city + "，状态：" + status);
            pageResult = logisticsOrderMapper.selectOrderPageByCityAndStatus(pageObj, city, status);
        } else if (city != null && !city.trim().isEmpty()) {
            // 只有城市条件
            System.out.println("调用selectOrderPageByCity，城市：" + city);
            pageResult = logisticsOrderMapper.selectOrderPageByCity(pageObj, city);
        } else if (status != null) {
            // 只有状态条件
            System.out.println("调用selectOrderPageByStatus，状态：" + status);
            pageResult = logisticsOrderMapper.selectOrderPageByStatus(pageObj, status);
        } else {
            // 无条件查询所有
            System.out.println("调用selectAllOrderPage");
            pageResult = logisticsOrderMapper.selectAllOrderPage(pageObj);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("current", pageResult.getCurrent());
        result.put("size", pageResult.getSize());
        result.put("pages", pageResult.getPages());
        
        return result;
    }

    @Override
    public int generateTestData(Integer count) {
        List<LogisticsOrder> orders = new ArrayList<>();
        String[] cities = {"深圳", "广州", "珠海", "东莞", "佛山", "中山", "惠州", "江门", "肇庆", "汕头"};
        String[] provinces = {"广东", "湖南", "湖北", "江西", "福建", "浙江", "江苏", "上海", "北京", "天津"};
        Long[] companyIds = {1L, 2L, 3L, 4L, 5L};
        
        Random random = new Random();
        
        for (int i = 0; i < count; i++) {
            LogisticsOrder order = new LogisticsOrder();
            order.setCompanyId(companyIds[random.nextInt(companyIds.length)]);
            order.setSenderName("发件人" + (i + 1));
            order.setSenderPhone("138" + String.format("%08d", random.nextInt(100000000)));
            order.setSenderCity(cities[random.nextInt(cities.length)]);
            order.setSenderProvince("广东");
            order.setSenderAddress("测试地址" + (i + 1));
            
            order.setReceiverName("收件人" + (i + 1));
            order.setReceiverPhone("139" + String.format("%08d", random.nextInt(100000000)));
            order.setReceiverCity(cities[random.nextInt(cities.length)]);
            order.setReceiverProvince(provinces[random.nextInt(provinces.length)]);
            order.setReceiverAddress("收件地址" + (i + 1));
            
            order.setPackageType(random.nextInt(2)); // 0或1
            order.setPackageWeight(BigDecimal.valueOf(random.nextDouble() * 10 + 0.1));
            order.setPackageValue(BigDecimal.valueOf(random.nextDouble() * 1000 + 10));
            order.setFreightCost(BigDecimal.valueOf(random.nextDouble() * 50 + 5));
            order.setOrderStatus(random.nextInt(6) + 1); // 1-6
            
            orders.add(order);
        }
        
        return batchCreateOrders(orders);
    }

    @Override
    public List<LogisticsStatistics> getStatistics(String startDate, String endDate, String province, String city) {
        QueryWrapper<LogisticsStatistics> wrapper = new QueryWrapper<>();
        
        if (startDate != null && !startDate.trim().isEmpty()) {
            wrapper.ge("stat_date", startDate);
        }
        
        if (endDate != null && !endDate.trim().isEmpty()) {
            wrapper.le("stat_date", endDate);
        }
        
        if (province != null && !province.trim().isEmpty()) {
            wrapper.eq("province", province);
        }
        
        if (city != null && !city.trim().isEmpty()) {
            wrapper.eq("city", city);
        }
        
        wrapper.orderByDesc("stat_date");
        
        return statisticsMapper.selectList(wrapper);
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000));
    }

    /**
     * 生成运单号
     */
    private String generateTrackingNo() {
        return "TRK" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000));
    }

    @Override
    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboardData = new HashMap<>();
        
        try {
            // 获取总包裹数量
            Integer totalPackages = getCurrentPackageCount();
            dashboardData.put("totalPackages", totalPackages);
            
            // 获取包裹类型占比并规范化为 { document: %, item: % }
            Map<String, Object> packageTypeRatioRaw = getPackageTypeRatio();
            Map<String, Object> packageTypeRatio = new HashMap<>();
            try {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) packageTypeRatioRaw.get("data");
                Number totalNum = (Number) packageTypeRatioRaw.getOrDefault("total", 0);
                int fileCount = 0;
                int itemCount = 0;
                if (dataList != null) {
                    for (Map<String, Object> item : dataList) {
                        String name = String.valueOf(item.get("name"));
                        Number valueNum = (Number) item.get("value");
                        int value = valueNum != null ? valueNum.intValue() : 0;
                        if ("文件".equals(name) || "document".equalsIgnoreCase(name)) {
                            fileCount = value;
                        } else if ("物品".equals(name) || "item".equalsIgnoreCase(name)) {
                            itemCount = value;
                        }
                    }
                }
                int total = totalNum != null ? totalNum.intValue() : (fileCount + itemCount);
                int documentPercent = total > 0 ? (int) Math.round(fileCount * 100.0 / total) : 0;
                int itemPercent = total > 0 ? (int) Math.round(itemCount * 100.0 / total) : 0;
                packageTypeRatio.put("document", documentPercent);
                packageTypeRatio.put("item", itemPercent);
            } catch (Exception e) {
                // 兜底：50/50
                packageTypeRatio.put("document", 50);
                packageTypeRatio.put("item", 50);
            }
            dashboardData.put("packageTypeRatio", packageTypeRatio);
            
            // 获取省份数据
            List<Map<String, Object>> provinceRaw = getProvinceData();
            List<Map<String, Object>> provinceData = new ArrayList<>();
            if (provinceRaw != null) {
                for (Map<String, Object> row : provinceRaw) {
                    Map<String, Object> normalized = new HashMap<>();
                    String name = String.valueOf(row.getOrDefault("province", row.getOrDefault("name", "")));
                    Number totalCount = (Number) row.getOrDefault("totalCount", row.getOrDefault("value", 0));
                    long value = totalCount != null ? totalCount.longValue() : 0L;
                    normalized.put("name", name);
                    normalized.put("value", value);
                    // 可选字段：packages（用于展示）
                    normalized.put("packages", value);
                    provinceData.add(normalized);
                }
            }
            dashboardData.put("provinceData", provinceData);
            
            // 获取城市排行
            List<Map<String, Object>> cityRaw = getCityRanking();
            List<Map<String, Object>> cityRanking = new ArrayList<>();
            if (cityRaw != null) {
                int rank = 1;
                for (Map<String, Object> row : cityRaw) {
                    Map<String, Object> normalized = new HashMap<>();
                    String name = String.valueOf(row.getOrDefault("city", row.getOrDefault("name", "")));
                    Number pkgCount = (Number) row.getOrDefault("packageCount", row.getOrDefault("packages", 0));
                    long packages = pkgCount != null ? pkgCount.longValue() : 0L;
                    normalized.put("name", name);
                    normalized.put("rank", rank++);
                    normalized.put("packages", packages);
                    // 增长率字段前端必需，用0%占位
                    normalized.put("growth", "0%");
                    cityRanking.add(normalized);
                }
            }
            dashboardData.put("cityRanking", cityRanking);
            
            // 获取时间序列数据
            List<Map<String, Object>> timeRaw = getTimeSeriesData();
            List<Map<String, Object>> timeSeriesData = new ArrayList<>();
            if (timeRaw != null) {
                for (Map<String, Object> row : timeRaw) {
                    Map<String, Object> normalized = new HashMap<>();
                    String time = String.valueOf(row.getOrDefault("date", row.getOrDefault("time", "")));
                    Number orderCount = (Number) row.getOrDefault("orderCount", row.getOrDefault("packages", 0));
                    long packages = orderCount != null ? orderCount.longValue() : 0L;
                    normalized.put("time", time);
                    normalized.put("packages", packages);
                    timeSeriesData.add(normalized);
                }
            }
            dashboardData.put("timeSeriesData", timeSeriesData);
            
            // 获取仓库数据
            Map<String, Object> warehouseData = getWarehouseData();
            dashboardData.put("warehouseData", warehouseData);
            
            // 获取收支数据
            Map<String, Object> profitData = getProfitData();
            dashboardData.put("profitData", profitData);
            
        } catch (Exception e) {
            System.err.println("获取大屏数据失败: " + e.getMessage());
            // 返回默认数据
            dashboardData = getDefaultDashboardData();
        }
        
        return dashboardData;
    }

    /**
     * 获取仓库数据
     */
    private Map<String, Object> getWarehouseData() {
        Map<String, Object> warehouseData = new HashMap<>();
        
        // 入库件数
         QueryWrapper<LogisticsOrder> inboundWrapper = new QueryWrapper<>();
         inboundWrapper.eq("order_status", 2); // 已揽收
         long inbound = logisticsOrderMapper.selectCount(inboundWrapper);
         warehouseData.put("inbound", inbound);
         
         // 在库件数
         QueryWrapper<LogisticsOrder> inStockWrapper = new QueryWrapper<>();
         inStockWrapper.in("order_status", 2, 3); // 已揽收、运输中
         long inStock = logisticsOrderMapper.selectCount(inStockWrapper);
         warehouseData.put("inStock", inStock);
         
         // 正常件数
         QueryWrapper<LogisticsOrder> normalWrapper = new QueryWrapper<>();
         normalWrapper.in("order_status", 2, 3, 4); // 正常流程中的订单
         long normal = logisticsOrderMapper.selectCount(normalWrapper);
         warehouseData.put("normal", normal);
         
         // 滞留件数
         QueryWrapper<LogisticsOrder> delayedWrapper = new QueryWrapper<>();
         delayedWrapper.eq("order_status", 6); // 异常状态
         long delayed = logisticsOrderMapper.selectCount(delayedWrapper);
         warehouseData.put("delayed", delayed);
         
         // 出库件数
         QueryWrapper<LogisticsOrder> outboundWrapper = new QueryWrapper<>();
         outboundWrapper.in("order_status", 4, 5); // 派送中、已签收
         long outbound = logisticsOrderMapper.selectCount(outboundWrapper);
         warehouseData.put("outbound", outbound);
         
         // 派送件数
         QueryWrapper<LogisticsOrder> deliveryWrapper = new QueryWrapper<>();
         deliveryWrapper.eq("order_status", 4); // 派送中
         long delivery = logisticsOrderMapper.selectCount(deliveryWrapper);
         warehouseData.put("delivery", delivery);
        
        // 自提件数（模拟数据）
        long pickup = Math.max(0, outbound - delivery);
        warehouseData.put("pickup", pickup);
        
        // 退签件数（模拟数据）
        long returned = delayed / 2;
        warehouseData.put("returned", returned);
        
        // 丢失件数（模拟数据）
        long lost = delayed / 10;
        warehouseData.put("lost", lost);
        
        return warehouseData;
    }

    /**
     * 获取收支数据
     */
    private Map<String, Object> getProfitData() {
        Map<String, Object> profitData = new HashMap<>();
        
        // 计算总收入（基于运费）
         QueryWrapper<LogisticsOrder> wrapper = new QueryWrapper<>();
         wrapper.select("IFNULL(SUM(freight_cost), 0) as total_income");
         List<Map<String, Object>> incomeResult = logisticsOrderMapper.selectMaps(wrapper);
        double income = 0;
        if (!incomeResult.isEmpty() && incomeResult.get(0).get("total_income") != null) {
            income = Double.parseDouble(incomeResult.get(0).get("total_income").toString());
        }
        
        // 模拟支出（约为收入的60%）
        double expense = income * 0.6;
        
        profitData.put("income", income);
        profitData.put("expense", expense);
        profitData.put("profit", income - expense);
        
        return profitData;
    }

    /**
     * 获取默认大屏数据（用于异常情况）
     */
    private Map<String, Object> getDefaultDashboardData() {
        Map<String, Object> defaultData = new HashMap<>();
        
        defaultData.put("totalPackages", 0);
        defaultData.put("packageTypeRatio", Map.of("document", 50, "item", 50));
        defaultData.put("provinceData", new ArrayList<>());
        defaultData.put("cityRanking", new ArrayList<>());
        defaultData.put("timeSeriesData", new ArrayList<>());
        defaultData.put("warehouseData", Map.of(
            "inbound", 0, "inStock", 0, "normal", 0, "delayed", 0,
            "outbound", 0, "delivery", 0, "pickup", 0, "returned", 0, "lost", 0
        ));
        defaultData.put("profitData", Map.of("income", 0, "expense", 0, "profit", 0));
        
        return defaultData;
    }

    @Override
    public Map<String, Object> getStatisticsSummary() {
        Map<String, Object> summary = new HashMap<>();
        // 总订单数
        long total = logisticsOrderMapper.selectCount(new QueryWrapper<>());
        summary.put("totalOrders", total);

        // 运输中（含已揽收、运输中、派送中）
        QueryWrapper<LogisticsOrder> inTransitWrapper = new QueryWrapper<>();
        inTransitWrapper.in("order_status", Arrays.asList(2, 3, 4));
        long inTransit = logisticsOrderMapper.selectCount(inTransitWrapper);
        summary.put("inTransitOrders", inTransit);

        // 已完成（已签收）
        QueryWrapper<LogisticsOrder> completedWrapper = new QueryWrapper<>();
        completedWrapper.eq("order_status", 5);
        long completed = logisticsOrderMapper.selectCount(completedWrapper);
        summary.put("completedOrders", completed);

        // 异常订单（异常）
        QueryWrapper<LogisticsOrder> exceptionWrapper = new QueryWrapper<>();
        exceptionWrapper.eq("order_status", 6);
        long exception = logisticsOrderMapper.selectCount(exceptionWrapper);
        summary.put("exceptionOrders", exception);

        return summary;
    }
}
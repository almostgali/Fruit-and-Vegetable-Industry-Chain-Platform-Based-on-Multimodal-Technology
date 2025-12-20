// 智慧物流服务中心动态大屏JavaScript

// 全局变量
let dashboardData = {};
let refreshInterval = null;
let websocket = null;

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    initializeDashboard();
    loadInitialData();
    loadStatisticsSummary();
    setupWebSocket();
    startAutoRefresh();
});

// 初始化大屏
function initializeDashboard() {
    console.log('初始化智慧物流大屏...');
    
    // 设置当前时间
    updateCurrentTime();
    setInterval(updateCurrentTime, 1000);
    
    // 初始化图表容器
    initializeCharts();
}

// 加载初始数据
async function loadInitialData() {
    try {
        const response = await fetch('/api/logistics/dashboard-data');
        if (response.ok) {
            const result = await response.json();
            if (result.success) {
                dashboardData = result.data;
                updateDashboard(dashboardData);
            }
        }
    } catch (error) {
        console.error('加载初始数据失败:', error);
        // 使用模拟数据
        loadMockData();
    }
}

// 加载模拟数据（用于演示）
function loadMockData() {
    dashboardData = {
        totalPackages: Math.floor(Math.random() * 1000000) + 500000,
        packageTypeRatio: {
            document: Math.floor(Math.random() * 40) + 30,
            item: Math.floor(Math.random() * 40) + 30
        },
        provinceData: generateProvinceData(),
        cityRanking: generateCityRanking(),
        timeSeriesData: generateTimeSeriesData(),
        warehouseData: generateWarehouseData(),
        profitData: {
            income: Math.floor(Math.random() * 500000) + 100000,
            expense: Math.floor(Math.random() * 300000) + 50000
        }
    };
    
    updateDashboard(dashboardData);
}

// 加载统计概要（总订单、运输中、已完成、异常）
async function loadStatisticsSummary() {
    try {
        const response = await fetch('/api/logistics/statistics/summary');
        if (response.ok) {
            const result = await response.json();
            if (result.success) {
                updateStatCards(result.data);
            }
        }
    } catch (error) {
        console.error('加载统计概要失败:', error);
    }
}

// 更新统计卡片显示
function updateStatCards(summary) {
    const map = [
        ['statTotalOrders', summary.totalOrders],
        ['statInTransitOrders', summary.inTransitOrders],
        ['statCompletedOrders', summary.completedOrders],
        ['statExceptionOrders', summary.exceptionOrders]
    ];
    map.forEach(([id, value]) => {
        const el = document.getElementById(id);
        if (el != null) {
            animateNumber(el, Number(value) || 0);
        }
    });
}

// 生成省份数据
function generateProvinceData() {
    const provinces = ['广东', '江苏', '浙江', '山东', '河南', '四川', '湖北', '福建', '湖南', '安徽'];
    return provinces.map(province => ({
        name: province,
        value: Math.floor(Math.random() * 50000) + 10000,
        packages: Math.floor(Math.random() * 10000) + 1000
    }));
}

// 生成城市排行数据
function generateCityRanking() {
    const cities = ['深圳', '广州', '东莞', '佛山', '中山', '珠海', '惠州', '江门', '肇庆', '汕头'];
    return cities.map((city, index) => ({
        rank: index + 1,
        name: city,
        packages: Math.floor(Math.random() * 20000) + 5000,
        growth: (Math.random() * 20 - 10).toFixed(1) + '%'
    }));
}

// 生成时间序列数据
function generateTimeSeriesData() {
    const data = [];
    const now = new Date();
    for (let i = 23; i >= 0; i--) {
        const time = new Date(now.getTime() - i * 60 * 60 * 1000);
        data.push({
            time: time.getHours() + ':00',
            packages: Math.floor(Math.random() * 5000) + 1000,
            income: Math.floor(Math.random() * 50000) + 10000
        });
    }
    return data;
}

// 生成仓库数据
function generateWarehouseData() {
    return {
        inbound: Math.floor(Math.random() * 10000) + 5000,
        inStock: Math.floor(Math.random() * 8000) + 3000,
        normal: Math.floor(Math.random() * 7000) + 2500,
        delayed: Math.floor(Math.random() * 1000) + 100,
        outbound: Math.floor(Math.random() * 9000) + 4000,
        delivery: Math.floor(Math.random() * 7000) + 3000,
        pickup: Math.floor(Math.random() * 2000) + 500,
        returned: Math.floor(Math.random() * 500) + 50,
        lost: Math.floor(Math.random() * 100) + 10
    };
}

// 更新大屏数据
function updateDashboard(data) {
    // 更新当前到件量
    updateTotalPackages(data.totalPackages);
    
    // 更新包裹类型占比
    updatePackageTypeRatio(data.packageTypeRatio);
    
    // 更新省份数据
    updateProvinceData(data.provinceData);
    
    // 更新城市排行
    updateCityRanking(data.cityRanking);
    
    // 更新时间序列图表
    updateTimeSeriesChart(data.timeSeriesData);
    
    // 更新仓库数据表格
    updateWarehouseTable(data.warehouseData);
    
    // 更新收支数据
    updateProfitData(data.profitData);
    
    console.log('大屏数据更新完成');
}

// 更新当前到件量
function updateTotalPackages(total) {
    const elements = document.querySelectorAll('.current-data, .pop-data-box p');
    elements.forEach(el => {
        if (el) {
            animateNumber(el, total);
        }
    });
}

// 更新包裹类型占比
function updatePackageTypeRatio(ratio) {
    // 更新饼图数据
    if (window.pie1Chart) {
        const option = {
            series: [{
                data: [
                    { value: ratio.document, name: '文件' },
                    { value: ratio.item, name: '物品' }
                ]
            }]
        };
        window.pie1Chart.setOption(option);
    }
    
    // 更新百分比显示
    const docElement = document.querySelector('.ratio-document');
    const itemElement = document.querySelector('.ratio-item');
    if (docElement) docElement.textContent = ratio.document + '%';
    if (itemElement) itemElement.textContent = ratio.item + '%';
}

// 更新省份数据
function updateProvinceData(provinceData) {
    // 更新地图数据
    if (window.mapChart) {
        const option = {
            series: [{
                data: provinceData.map(item => ({
                    name: item.name,
                    value: item.value
                }))
            }]
        };
        window.mapChart.setOption(option);
    }
}

// 更新城市排行
function updateCityRanking(cityData) {
    const rankingContainer = document.querySelector('.ranking-list, .city-ranking');
    if (rankingContainer) {
        rankingContainer.innerHTML = '';
        cityData.forEach(city => {
            const item = document.createElement('div');
            item.className = 'ranking-item';
            item.innerHTML = `
                <span class="rank">${city.rank}</span>
                <span class="city-name">${city.name}</span>
                <span class="packages">${formatNumber(city.packages)}</span>
                <span class="growth ${city.growth.includes('-') ? 'negative' : 'positive'}">${city.growth}</span>
            `;
            rankingContainer.appendChild(item);
        });
    }
}

// 更新时间序列图表
function updateTimeSeriesChart(timeData) {
    if (window.lineChart) {
        const option = {
            xAxis: {
                data: timeData.map(item => item.time)
            },
            series: [{
                data: timeData.map(item => item.packages)
            }]
        };
        window.lineChart.setOption(option);
    }
}

// 更新仓库数据表格
function updateWarehouseTable(warehouseData) {
    // 更新派件数据表格
    const deliveryElements = {
        '.dph-data1': warehouseData.inbound,
        '.dph-data2': warehouseData.inStock,
        '.dph-data3': warehouseData.normal,
        '.dph-data5': warehouseData.delayed,
        '.dph-data6': warehouseData.outbound,
        '.dph-data7': warehouseData.delivery,
        '.dph-data8': warehouseData.pickup,
        '.dph-data9': warehouseData.returned,
        '.dph-data4': warehouseData.lost
    };
    
    Object.entries(deliveryElements).forEach(([selector, value]) => {
        const element = document.querySelector(selector);
        if (element) {
            animateNumber(element, value);
        }
    });
    
    // 更新寄件数据表格
    const mailElements = {
        '.mail-data1': Math.floor(warehouseData.inbound * 0.8),
        '.mail-data2': Math.floor(warehouseData.inStock * 0.7),
        '.mail-data7': Math.floor(warehouseData.normal * 0.7),
        '.mail-data4': Math.floor(warehouseData.delayed * 0.5),
        '.mail-data6': Math.floor(warehouseData.outbound * 0.6),
        '.mail-data3': Math.floor(warehouseData.lost * 0.3),
        '.mail-data5': Math.floor(warehouseData.returned * 0.4)
    };
    
    Object.entries(mailElements).forEach(([selector, value]) => {
        const element = document.querySelector(selector);
        if (element) {
            animateNumber(element, value);
        }
    });
}

// 更新收支数据
function updateProfitData(profitData) {
    const totalProfitElement = document.getElementById('totalProfit');
    if (totalProfitElement) {
        const profit = profitData.income - profitData.expense;
        totalProfitElement.textContent = formatCurrency(profit);
    }
}

// 设置WebSocket连接
function setupWebSocket() {
    try {
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const wsUrl = `${protocol}//${window.location.host}/ws/logistics-dashboard`;
        
        websocket = new WebSocket(wsUrl);
        
        websocket.onopen = function() {
            console.log('WebSocket连接已建立');
        };
        
        websocket.onmessage = function(event) {
            try {
                const data = JSON.parse(event.data);
                if (data.type === 'dashboard-update') {
                    dashboardData = { ...dashboardData, ...data.data };
                    updateDashboard(dashboardData);
                } else if (data.type === 'statistics-update') {
                    updateStatCards(data.data);
                }
            } catch (error) {
                console.error('处理WebSocket消息失败:', error);
            }
        };
        
        websocket.onclose = function() {
            console.log('WebSocket连接已关闭，尝试重连...');
            setTimeout(setupWebSocket, 5000);
        };
        
        websocket.onerror = function(error) {
            console.error('WebSocket错误:', error);
        };
    } catch (error) {
        console.error('WebSocket连接失败:', error);
    }
}

// 开始自动刷新
function startAutoRefresh() {
    // 每30秒刷新一次数据
    refreshInterval = setInterval(() => {
        loadInitialData();
        loadStatisticsSummary();
    }, 30000);
}

// 停止自动刷新
function stopAutoRefresh() {
    if (refreshInterval) {
        clearInterval(refreshInterval);
        refreshInterval = null;
    }
}

// 初始化图表
function initializeCharts() {
    // 这里可以初始化ECharts图表
    // 由于原始代码可能使用了特定的图表库，这里提供基础框架
    console.log('初始化图表容器');
}

// 数字动画效果
function animateNumber(element, targetValue, duration = 1000) {
    if (!element) return;
    
    const startValue = parseInt(element.textContent.replace(/[^\d]/g, '')) || 0;
    const difference = targetValue - startValue;
    const startTime = Date.now();
    
    function updateNumber() {
        const elapsed = Date.now() - startTime;
        const progress = Math.min(elapsed / duration, 1);
        
        const currentValue = Math.floor(startValue + difference * progress);
        element.textContent = formatNumber(currentValue);
        
        if (progress < 1) {
            requestAnimationFrame(updateNumber);
        }
    }
    
    updateNumber();
}

// 格式化数字
function formatNumber(num) {
    if (num >= 1000000) {
        return (num / 1000000).toFixed(1) + 'M';
    } else if (num >= 1000) {
        return (num / 1000).toFixed(1) + 'K';
    }
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}

// 格式化货币
function formatCurrency(amount) {
    return amount.toLocaleString('zh-CN', {
        style: 'currency',
        currency: 'CNY',
        minimumFractionDigits: 1,
        maximumFractionDigits: 1
    });
}

// 更新当前时间
function updateCurrentTime() {
    const now = new Date();
    const timeString = now.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });
    
    const timeElements = document.querySelectorAll('.current-time, #date_a');
    timeElements.forEach(el => {
        if (el) {
            el.textContent = timeString;
        }
    });
}

// 页面卸载时清理资源
window.addEventListener('beforeunload', function() {
    stopAutoRefresh();
    if (websocket) {
        websocket.close();
    }
});

// 暴露全局函数供其他脚本使用
window.DynamicDashboard = {
    updateDashboard,
    loadInitialData,
    loadStatisticsSummary,
    formatNumber,
    formatCurrency
};
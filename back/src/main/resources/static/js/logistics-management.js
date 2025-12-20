// 物流管理平台JavaScript功能

// 全局变量
let currentPage = 1;
let pageSize = 10;

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    initializePage();
    loadOrderList();
    loadStatistics();
    setupStatisticsWebSocket();
});

// 初始化页面
function initializePage() {
    // 标签页切换
    const navBtns = document.querySelectorAll('.nav-btn');
    const tabContents = document.querySelectorAll('.tab-content');
    
    navBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const tabName = this.getAttribute('data-tab');
            
            // 移除所有活动状态
            navBtns.forEach(b => b.classList.remove('active'));
            tabContents.forEach(t => t.classList.remove('active'));
            
            // 添加当前活动状态
            this.classList.add('active');
            document.getElementById(tabName + '-tab').classList.add('active');
            
            // 根据标签页加载相应数据
            if (tabName === 'order') {
                loadOrderList();
            } else if (tabName === 'statistics') {
                loadStatistics();
            }
        });
    });
    
    // 订单表单提交
    const orderForm = document.getElementById('orderForm');
    orderForm.addEventListener('submit', handleOrderSubmit);

    // 实时大屏操作按钮自动设置ID，便于控制禁用与文案
    const dashboardActions = document.querySelector('.dashboard-actions');
    if (dashboardActions) {
        const buttons = dashboardActions.querySelectorAll('button');
        if (buttons[0] && !buttons[0].id) buttons[0].id = 'btnOpenDashboard';
        if (buttons[1] && !buttons[1].id) buttons[1].id = 'btnRefreshDashboard';
    }
}

// 处理订单提交
async function handleOrderSubmit(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const orderData = {
        companyId: parseInt(formData.get('companyId')),
        packageType: parseInt(formData.get('packageType')),
        senderName: formData.get('senderName'),
        senderPhone: formData.get('senderPhone'),
        senderProvince: formData.get('senderProvince'),
        senderCity: formData.get('senderCity'),
        senderAddress: formData.get('senderAddress'),
        receiverName: formData.get('receiverName'),
        receiverPhone: formData.get('receiverPhone'),
        receiverProvince: formData.get('receiverProvince'),
        receiverCity: formData.get('receiverCity'),
        receiverAddress: formData.get('receiverAddress'),
        packageWeight: parseFloat(formData.get('packageWeight')) || 0,
        packageValue: parseFloat(formData.get('packageValue')) || 0,
        freightCost: parseFloat(formData.get('freightCost')) || 0
    };
    
    try {
        const response = await axios.post('/api/logistics/order', orderData);
        if (response.data.success) {
            showMessage('订单创建成功！', 'success');
            event.target.reset();
            loadOrderList();
            loadStatistics();
        } else {
            showMessage('订单创建失败：' + response.data.message, 'error');
        }
    } catch (error) {
        console.error('创建订单失败:', error);
        showMessage('网络错误，请稍后重试', 'error');
    }
}

// 加载订单列表
async function loadOrderList() {
    const cityFilter = document.getElementById('cityFilter')?.value || '';
    const statusFilter = document.getElementById('statusFilter')?.value || '';
    
    try {
        const params = {
            page: currentPage,
            size: pageSize
        };
        
        if (cityFilter) params.city = cityFilter;
        if (statusFilter) params.status = statusFilter;
        
        const response = await axios.get('/api/logistics/order/list', { params });
        
        if (response.data.success) {
            renderOrderTable(response.data.data.records);
            renderPagination(response.data.data);
        }
    } catch (error) {
        console.error('加载订单列表失败:', error);
        showMessage('加载订单列表失败', 'error');
    }
}

// 渲染订单表格
function renderOrderTable(orders) {
    const tbody = document.getElementById('orderTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    orders.forEach(order => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${order.orderNo}</td>
            <td>${order.trackingNo}</td>
            <td>${order.senderCity}</td>
            <td>${order.receiverCity}</td>
            <td>${order.packageType === 0 ? '文件' : '物品'}</td>
            <td><span class="status-badge status-${order.orderStatus}">${getStatusText(order.orderStatus)}</span></td>
            <td>${formatDateTime(order.createdTime)}</td>
            <td>
                <button class="btn btn-sm" onclick="updateOrderStatus(${order.id}, ${order.orderStatus})">更新状态</button>
                <button class="btn btn-sm" onclick="viewOrderDetail('${order.orderNo}')">详情</button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// 渲染分页
function renderPagination(pageData) {
    const pagination = document.getElementById('pagination');
    if (!pagination) return;
    
    pagination.innerHTML = '';
    
    const totalPages = pageData.pages;
    const current = pageData.current;
    
    // 上一页
    if (current > 1) {
        const prevBtn = createPageButton('上一页', current - 1);
        pagination.appendChild(prevBtn);
    }
    
    // 页码
    for (let i = Math.max(1, current - 2); i <= Math.min(totalPages, current + 2); i++) {
        const pageBtn = createPageButton(i, i);
        if (i === current) pageBtn.classList.add('active');
        pagination.appendChild(pageBtn);
    }
    
    // 下一页
    if (current < totalPages) {
        const nextBtn = createPageButton('下一页', current + 1);
        pagination.appendChild(nextBtn);
    }
}

// 创建分页按钮
function createPageButton(text, page) {
    const button = document.createElement('button');
    button.textContent = text;
    button.onclick = () => {
        currentPage = page;
        loadOrderList();
    };
    return button;
}

// 更新订单状态
async function updateOrderStatus(orderId, currentStatus) {
    const statusOptions = [
        { value: 1, text: '已下单' },
        { value: 2, text: '已揽收' },
        { value: 3, text: '运输中' },
        { value: 4, text: '派送中' },
        { value: 5, text: '已签收' },
        { value: 6, text: '异常' }
    ];
    
    const nextStatus = currentStatus < 5 ? currentStatus + 1 : currentStatus;
    const statusText = statusOptions.find(s => s.value === nextStatus)?.text || '未知';
    
    if (confirm(`确定将订单状态更新为"${statusText}"吗？`)) {
        try {
            const response = await axios.put(`/api/logistics/order/${orderId}/status`, null, { params: { status: nextStatus } });
            
            if (response.data.success) {
                showMessage('状态更新成功', 'success');
                loadOrderList();
                loadStatistics();
            } else {
                showMessage('状态更新失败：' + response.data.message, 'error');
            }
        } catch (error) {
            console.error('更新状态失败:', error);
            showMessage('网络错误，请稍后重试', 'error');
        }
    }
}

// 查看订单详情
function viewOrderDetail(orderNumber) {
    // 这里可以实现订单详情弹窗
    showMessage(`查看订单 ${orderNumber} 详情功能待实现`, 'info');
}

// 生成测试数据
async function generateTestData() {
    const count = document.getElementById('testDataCount')?.value || 50;
    const resultDiv = document.getElementById('batchResult');
    
    if (resultDiv) {
        resultDiv.innerHTML = '<div class="loading">正在生成测试数据...</div>';
    }
    
    try {
        const response = await axios.post(`/api/logistics/test/generate?count=${count}`);
        
        if (response.data.success) {
            showMessage(`成功生成 ${count} 条测试数据`, 'success');
            if (resultDiv) {
                resultDiv.innerHTML = `
                    <div class="success-message">
                        <h4>数据生成成功</h4>
                        <p>已生成 ${count} 条测试订单数据</p>
                        <p>生成时间: ${new Date().toLocaleString()}</p>
                    </div>
                `;
            }
            loadOrderList();
            loadStatistics();
        } else {
            showMessage('生成测试数据失败：' + response.data.message, 'error');
            if (resultDiv) {
                resultDiv.innerHTML = '<div class="error-message">数据生成失败</div>';
            }
        }
    } catch (error) {
        console.error('生成测试数据失败:', error);
        showMessage('网络错误，请稍后重试', 'error');
        if (resultDiv) {
            resultDiv.innerHTML = '<div class="error-message">网络错误</div>';
        }
    }
}

// 加载统计数据
async function loadStatistics() {
    try {
        const response = await axios.get('/api/logistics/statistics/summary');
        
        if (response.data.success) {
            const stats = response.data.data || {};
            
            // 更新统计卡片
            updateStatCard('totalOrders', stats.totalOrders || 0);
            updateStatCard('inTransitOrders', stats.inTransitOrders || 0);
            updateStatCard('completedOrders', stats.completedOrders || 0);
            updateStatCard('exceptionOrders', stats.exceptionOrders || 0);
        }
    } catch (error) {
        console.error('加载统计数据失败:', error);
    }
}

// 统计WebSocket：实时接收统计概要并更新卡片
function setupStatisticsWebSocket() {
    try {
        const protocol = location.protocol === 'https:' ? 'wss' : 'ws';
        const wsUrl = `${protocol}://${location.host}/ws/logistics-dashboard`;
        const socket = new WebSocket(wsUrl);

        socket.addEventListener('open', () => {
            console.log('统计WebSocket连接已建立');
        });

        socket.addEventListener('message', (event) => {
            try {
                const msg = JSON.parse(event.data);
                if (msg && msg.type === 'statistics-update' && msg.data) {
                    const stats = msg.data || {};
                    updateStatCard('totalOrders', stats.totalOrders || 0);
                    updateStatCard('inTransitOrders', stats.inTransitOrders || 0);
                    updateStatCard('completedOrders', stats.completedOrders || 0);
                    updateStatCard('exceptionOrders', stats.exceptionOrders || 0);
                }
            } catch (e) {
                console.warn('统计消息解析失败:', e);
            }
        });

        socket.addEventListener('close', () => {
            console.log('统计WebSocket连接已关闭');
        });

        socket.addEventListener('error', (e) => {
            console.error('统计WebSocket错误:', e);
        });
    } catch (e) {
        console.error('初始化统计WebSocket失败:', e);
    }
}

// 更新统计卡片
function updateStatCard(elementId, value) {
    const element = document.getElementById(elementId);
    if (element) {
        // 数字动画效果
        animateNumber(element, parseInt(element.textContent) || 0, value, 1000);
    }
}

// 数字动画
function animateNumber(element, start, end, duration) {
    const range = end - start;
    const increment = range / (duration / 16);
    let current = start;
    
    const timer = setInterval(() => {
        current += increment;
        if ((increment > 0 && current >= end) || (increment < 0 && current <= end)) {
            current = end;
            clearInterval(timer);
        }
        element.textContent = Math.floor(current);
    }, 16);
}

// 打开大屏
function openDashboard() {
    const btnCandidates = Array.from(document.querySelectorAll('.dashboard-actions button'));
    const btn = document.getElementById('btnOpenDashboard') || btnCandidates.find(b => /打开/.test(b.textContent));
    if (btn) {
        btn.disabled = true;
        btn.dataset.prevText = btn.textContent;
        btn.textContent = '打开中...';
    }

    showMessage('正在打开大屏...', 'info');

    let newWindow = null;
    try {
        newWindow = window.open('/001智慧物流服务中心/index.html', '_blank');
    } catch (e) {
        console.error('打开大屏失败:', e);
    }

    if (!newWindow) {
        const iframe = document.getElementById('dashboardFrame');
        if (iframe) {
            iframe.src = '/001智慧物流服务中心/index.html';
            showMessage('弹窗被拦截，已在下方嵌入打开', 'info');
        } else {
            showMessage('无法打开大屏，请稍后重试', 'error');
        }
    } else {
        showMessage('已在新标签页打开大屏', 'success');
    }

    setTimeout(() => {
        if (btn) {
            btn.disabled = false;
            btn.textContent = btn.dataset.prevText || '打开大屏';
            delete btn.dataset.prevText;
        }
    }, 500);
}

// 刷新大屏数据
async function refreshDashboard() {
    const btnCandidates = Array.from(document.querySelectorAll('.dashboard-actions button'));
    const btn = document.getElementById('btnRefreshDashboard') || btnCandidates.find(b => /刷新/.test(b.textContent));
    if (btn) {
        btn.disabled = true;
        btn.dataset.prevText = btn.textContent;
        btn.textContent = '刷新中...';
    }

    showMessage('正在刷新大屏数据...', 'info');

    try {
        const response = await axios.post('/api/logistics/refresh-dashboard');
        if (response?.data?.success) {
            showMessage('大屏数据刷新成功', 'success');
            const iframe = document.getElementById('dashboardFrame');
            if (iframe) {
                iframe.src = iframe.src;
            }
        } else {
            showMessage('刷新失败：' + (response?.data?.message || '未知错误'), 'error');
        }
    } catch (error) {
        console.error('刷新大屏数据失败:', error);
        showMessage('刷新失败，请稍后重试', 'error');
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.textContent = btn.dataset.prevText || '刷新数据';
            delete btn.dataset.prevText;
        }
    }
}

// 获取状态文本
function getStatusText(status) {
    const statusMap = {
        1: '已下单',
        2: '已揽收',
        3: '运输中',
        4: '派送中',
        5: '已签收',
        6: '异常'
    };
    return statusMap[status] || '未知';
}

// 格式化日期时间
function formatDateTime(dateTime) {
    if (!dateTime) return '';
    const date = new Date(dateTime);
    return date.toLocaleString('zh-CN');
}

// 显示消息提示
function showMessage(message, type = 'info') {
    const messageDiv = document.getElementById('message');
    if (!messageDiv) return;
    
    messageDiv.textContent = message;
    messageDiv.className = `message ${type}`;
    messageDiv.classList.add('show');
    
    setTimeout(() => {
        messageDiv.classList.remove('show');
    }, 3000);
}

// 退出登录
async function logout() {
    try {
        const response = await axios.post('/logistics/logout');
        if (response.data === 'success' || response.data?.success) {
            showMessage('已退出登录', 'success');
            setTimeout(() => {
                window.location.href = '/traceability';
            }, 500);
        } else {
            showMessage('退出登录失败', 'error');
        }
    } catch (error) {
        console.error('退出登录失败:', error);
        showMessage('网络错误，请稍后重试', 'error');
    }
}

// 添加状态样式
const style = document.createElement('style');
style.textContent = `
    .status-badge {
        padding: 4px 8px;
        border-radius: 12px;
        font-size: 12px;
        font-weight: 500;
    }
    .status-1 { background: #ffeaa7; color: #2d3436; }
    .status-2 { background: #74b9ff; color: white; }
    .status-3 { background: #fd79a8; color: white; }
    .status-4 { background: #fdcb6e; color: #2d3436; }
    .status-5 { background: #00b894; color: white; }
    .status-6 { background: #e17055; color: white; }
    
    .btn-sm {
        padding: 4px 8px;
        font-size: 12px;
        margin-right: 5px;
    }
    
    .loading {
        text-align: center;
        padding: 20px;
        color: #6c757d;
    }
    
    .success-message {
        color: #28a745;
        text-align: center;
    }
    
    .error-message {
        color: #dc3545;
        text-align: center;
        padding: 20px;
    }
`;
document.head.appendChild(style);
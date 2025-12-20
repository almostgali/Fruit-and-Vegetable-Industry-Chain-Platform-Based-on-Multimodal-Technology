// 全局变量
let currentPage = 'home';
let currentImageIndex = 0;
let detectionData = [];

// 页面初始化
document.addEventListener('DOMContentLoaded', function() {
    initializeCarousel();
    initializeNavigation();
    initializeDetection();
});

// 登录验证
function validateLogin() {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    
    if (!username || !password) {
        alert('请输入用户名和密码');
        return false;
    }
    
    // 这里可以添加实际的登录验证逻辑
    // 暂时使用简单验证
    if (username === 'admin' && password === '123456') {
        alert('登录成功！');
        window.location.href = '/index';
        return true;
    } else {
        alert('用户名或密码错误！');
        return false;
    }
}

// 用户注册
function registerUser() {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const email = document.getElementById('email').value;
    
    if (!username || !password || !confirmPassword || !email) {
        alert('请填写所有字段');
        return false;
    }
    
    if (password !== confirmPassword) {
        alert('两次输入的密码不一致');
        return false;
    }
    
    if (password.length < 6) {
        alert('密码长度至少6位');
        return false;
    }
    
    // 这里可以添加实际的注册逻辑
    alert('注册成功！请登录');
    window.location.href = '/login';
    return true;
}

// 页面导航
function showPage(pageId) {
    // 隐藏所有页面
    const pages = document.querySelectorAll('.page');
    pages.forEach(page => {
        page.classList.remove('active');
        page.style.display = 'none';
    });
    
    // 显示指定页面
    const targetPage = document.getElementById(pageId);
    if (targetPage) {
        targetPage.classList.add('active');
        targetPage.style.display = 'block';
    }
    
    currentPage = pageId;
}

// 返回首页
function goHome() {
    window.location.href = '/index';
}

// 退出登录
function logout() {
    if (confirm('确定要退出登录吗？')) {
        window.location.href = '/login';
    }
}

// 轮播图功能
function initializeCarousel() {
    const carousel = document.querySelector('.carousel');
    if (!carousel) return;
    
    const items = carousel.querySelectorAll('.carousel-item');
    const prevBtn = carousel.querySelector('.carousel-controls.prev');
    const nextBtn = carousel.querySelector('.carousel-controls.next');
    
    if (items.length === 0) return;
    
    let currentIndex = 0;
    
    function showSlide(index) {
        items.forEach((item, i) => {
            item.classList.toggle('active', i === index);
        });
    }
    
    function nextSlide() {
        currentIndex = (currentIndex + 1) % items.length;
        showSlide(currentIndex);
    }
    
    function prevSlide() {
        currentIndex = (currentIndex - 1 + items.length) % items.length;
        showSlide(currentIndex);
    }
    
    if (nextBtn) nextBtn.addEventListener('click', nextSlide);
    if (prevBtn) prevBtn.addEventListener('click', prevSlide);
    
    // 自动轮播
    setInterval(nextSlide, 5000);
    
    // 初始化显示第一张
    showSlide(0);
}

// 导航功能
function initializeNavigation() {
    const navLinks = document.querySelectorAll('.nav-links a');
    navLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            // 移除所有active类
            navLinks.forEach(l => l.classList.remove('active'));
            // 添加active类到当前链接
            this.classList.add('active');
        });
    });
}

// 检测功能初始化
function initializeDetection() {
    // 图片上传处理
    const uploadAreas = document.querySelectorAll('.upload-area');
    uploadAreas.forEach(area => {
        const input = area.querySelector('input[type="file"]');
        if (input) {
            area.addEventListener('click', () => input.click());
            input.addEventListener('change', handleImageUpload);
            
            // 拖拽上传
            area.addEventListener('dragover', handleDragOver);
            area.addEventListener('drop', handleDrop);
        }
    });
}

// 处理图片上传
function handleImageUpload(event) {
    const file = event.target.files[0];
    if (file && file.type.startsWith('image/')) {
        const reader = new FileReader();
        reader.onload = function(e) {
            displayUploadedImage(e.target.result, file.name);
        };
        reader.readAsDataURL(file);
    }
}

// 显示上传的图片
function displayUploadedImage(imageSrc, fileName) {
    const uploadArea = event.target.closest('.upload-area');
    if (uploadArea) {
        uploadArea.innerHTML = `
            <div class="image-preview">
                <img src="${imageSrc}" alt="上传的图片" style="max-width: 100%; max-height: 300px;">
                <p>${fileName}</p>
            </div>
        `;
    }
}

// 拖拽处理
function handleDragOver(event) {
    event.preventDefault();
    event.currentTarget.classList.add('dragover');
}

function handleDrop(event) {
    event.preventDefault();
    event.currentTarget.classList.remove('dragover');
    
    const files = event.dataTransfer.files;
    if (files.length > 0 && files[0].type.startsWith('image/')) {
        const reader = new FileReader();
        reader.onload = function(e) {
            displayUploadedImage(e.target.result, files[0].name);
        };
        reader.readAsDataURL(files[0]);
    }
}

// 开始检测
function startDetection(type) {
    const uploadArea = document.querySelector('.upload-area img');
    const fileInput = document.getElementById('image-upload');
    
    if (!uploadArea || !fileInput || !fileInput.files[0]) {
        alert('请先上传图片');
        return;
    }
    
    // 显示加载状态
    const detectBtn = event.target;
    const originalText = detectBtn.textContent;
    detectBtn.innerHTML = '<span class="loading"></span> 检测中...';
    detectBtn.disabled = true;
    
    // 创建FormData对象
    const formData = new FormData();
    formData.append('file', fileInput.files[0]);
    
    // 确定API端点
    const apiUrl = type === 'quality' ? '/api/model/detect/quality' : '/api/model/detect/maturity';
    
    // 发送请求到后端
    fetch(apiUrl, {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            displayDetectionResult(data.data, type);
        } else {
            alert('检测失败: ' + (data.message || '未知错误'));
        }
    })
    .catch(error => {
        console.error('检测请求失败:', error);
        alert('检测失败: 网络错误或服务器异常');
    })
    .finally(() => {
        // 恢复按钮状态
        detectBtn.textContent = originalText;
        detectBtn.disabled = false;
    });
}

// 模拟检测结果
function simulateDetection(type) {
    const uploadArea = document.querySelector('.upload-area img');
    const imageName = uploadArea.alt || '';
    
    if (type === 'quality') {
        // 根据图片名称模拟品质检测结果
        if (imageName.includes('优质') || imageName.includes('好')) {
            return {
                quality: '优质',
                freshness: '95%',
                defects: '无明显缺陷',
                grade: 'A级'
            };
        } else if (imageName.includes('一般') || imageName.includes('中等')) {
            return {
                quality: '良好',
                freshness: '80%',
                defects: '轻微瑕疵',
                grade: 'B级'
            };
        } else {
            return {
                quality: '优质',
                freshness: '90%',
                defects: '无明显缺陷',
                grade: 'A级'
            };
        }
    } else if (type === 'maturity') {
        // 根据图片名称模拟成熟度检测结果
        if (imageName.includes('成熟') || imageName.includes('熟')) {
            return {
                maturity: '完全成熟',
                ripeness: '95%',
                harvestTime: '适宜采摘',
                shelfLife: '7-10天'
            };
        } else if (imageName.includes('未成熟') || imageName.includes('生')) {
            return {
                maturity: '未成熟',
                ripeness: '60%',
                harvestTime: '需要3-5天',
                shelfLife: '15-20天'
            };
        } else {
            return {
                maturity: '适度成熟',
                ripeness: '85%',
                harvestTime: '适宜采摘',
                shelfLife: '10-12天'
            };
        }
    }
}

// 显示检测结果
function displayDetectionResult(result, type) {
    let resultContainer = document.querySelector('.detection-result');
    if (!resultContainer) {
        resultContainer = document.createElement('div');
        resultContainer.className = 'detection-result';
        document.querySelector('.detection-container').appendChild(resultContainer);
    }
    
    let resultHTML = '<h3>检测结果</h3>';
    
    Object.entries(result).forEach(([key, value]) => {
        const labels = {
            quality: '品质等级',
            freshness: '新鲜度',
            defects: '缺陷检测',
            grade: '等级评定',
            maturity: '成熟度',
            ripeness: '成熟程度',
            harvestTime: '采摘建议',
            shelfLife: '保质期'
        };
        
        resultHTML += `
            <div class="result-item">
                <span class="result-label">${labels[key] || key}:</span>
                <span class="result-value">${value}</span>
            </div>
        `;
    });
    
    resultHTML += `
        <div style="margin-top: 2rem; text-align: center;">
            <button onclick="exportData('${type}')" class="btn">导出数据</button>
        </div>
    `;
    
    resultContainer.innerHTML = resultHTML;
    
    // 保存检测数据
    const timestamp = new Date().toLocaleString();
    detectionData.push({
        timestamp,
        type,
        ...result
    });
}

// 导出数据
function exportData(type) {
    if (detectionData.length === 0) {
        alert('没有可导出的数据');
        return;
    }
    
    // 创建CSV内容
    const headers = ['时间', '检测类型'];
    const typeData = detectionData.filter(item => item.type === type);
    
    if (typeData.length === 0) {
        alert('没有该类型的检测数据');
        return;
    }
    
    // 获取所有可能的字段
    const allFields = new Set();
    typeData.forEach(item => {
        Object.keys(item).forEach(key => {
            if (key !== 'timestamp' && key !== 'type') {
                allFields.add(key);
            }
        });
    });
    
    headers.push(...Array.from(allFields));
    
    let csvContent = headers.join(',') + '\n';
    
    typeData.forEach(item => {
        const row = [item.timestamp, item.type];
        allFields.forEach(field => {
            row.push(item[field] || '');
        });
        csvContent += row.join(',') + '\n';
    });
    
    // 下载CSV文件
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `${type}_detection_data.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

// 工具函数
function formatDate(date) {
    return new Date(date).toLocaleString('zh-CN');
}

function showLoading(element) {
    element.innerHTML = '<span class="loading"></span> 处理中...';
    element.disabled = true;
}

function hideLoading(element, originalText) {
    element.textContent = originalText;
    element.disabled = false;
}
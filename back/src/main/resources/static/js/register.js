function register() {
    const username = document.getElementById('reg-username').value;
    const password = document.getElementById('reg-password').value;
    const confirm = document.getElementById('confirm-password').value;
    const email = document.getElementById('email').value;

    // 简单前端验证
    if(password !== confirm) {
        alert('两次输入的密码不一致！');
        return;
    }
    if(!username || !password){
        alert('用户名和密码不能为空！');
        return;
    }
    // 这里添加实际注册逻辑（需要后端支持）
    alert('注册成功，请登录！');
    window.location.href = '/login';
}

function showPage(pageId) {
    const pages = document.querySelectorAll('.page');
    pages.forEach(page => {
        page.style.display = 'none';
    });
    const selectedPage = document.getElementById(pageId);
    selectedPage.style.display = 'block';
}
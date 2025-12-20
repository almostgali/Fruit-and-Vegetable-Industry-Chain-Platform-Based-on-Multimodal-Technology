function login() {
  const username = document.getElementById('username').value;
  const password = document.getElementById('password').value;
  // 简单模拟登录验证，实际应用中应与后端交互
  if (username === 'admin' && password === 'admin') {
    // 隐藏登录页面
    document.getElementById('login-page').style.display = 'none';
    // 显示导航栏
    document.querySelector('nav').style.display = 'block';
    // 显示首页
    showPage('fruit-home');
  } else {
    alert('用户名或密码错误');
  }
}

function tologin() {
  document.getElementById('login-page').style.display = 'block';
}

function showPage(pageId) {
  const pages = document.querySelectorAll('.page');
  pages.forEach(page => {
    page.style.display = 'none';
  });
  const selectedPage = document.getElementById(pageId);
  selectedPage.style.display = 'block';
}
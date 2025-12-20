function login() {
  const username = document.getElementById('username').value;
  const password = document.getElementById('password').value;
  // 简单模拟登录验证，实际应用中应与后端交互
  if (username === 'admin' && password === 'admin') {
    // 隐藏登录页面
    document.getElementById('login-page').style.display = 'none';
    // 显示导航栏
    document.querySelector('nav').style.display = 'block';
    // 显示发货列表页面
    showPage('fruit-home');
  } else {
    alert('用户名或密码错误');
  }
}
const navLinks = document.querySelectorAll('nav ul li a');
navLinks.forEach(link => {
  link.addEventListener('click', function () {
    navLinks.forEach(navLink => navLink.classList.remove('active'));
    this.classList.add('active');
  });
});
function showPage(pageId) {
  console.log(pageId);
  if (pageId === 'fruit-maturity') {
    const pages = document.querySelectorAll('.page');
    pages.forEach(page => {
      page.style.display = 'none';
    });
    const selectedPage = document.getElementById(pageId);
    selectedPage.style.display = 'block';
    document.querySelector('nav').style.display = 'none';
  } else {
    const pages = document.querySelectorAll('.page');
    pages.forEach(page => {
      page.style.display = 'none';
    });
    const selectedPage = document.getElementById(pageId);
    selectedPage.style.display = 'block';
  }
  if (pageId === 'fruit-quality') {
    const pages = document.querySelectorAll('.page');
    pages.forEach(page => {
      page.style.display = 'none';
    });
    const selectedPage = document.getElementById(pageId);
    selectedPage.style.display = 'block';
    document.querySelector('nav').style.display = 'none';
  } else {
    const pages = document.querySelectorAll('.page');
    pages.forEach(page => {
      page.style.display = 'none';
    });
    const selectedPage = document.getElementById(pageId);
    selectedPage.style.display = 'block';
  }
  
    
}
// 退回首页
function returnHome() {
  showPage('fruit-home');
  document.querySelector('nav').style.display = 'block';
  // 清除所有高亮
  document.querySelectorAll('nav ul li a').forEach(link => link.classList.remove('active'));
  // 给菜单第一个加高亮
  document.querySelector('nav ul li a').classList.add('active');
}
// 退出登录
function tologin() {
  document.getElementById('login-page').style.display = 'block';
  document.querySelector('nav').style.display = 'none';
  document.getElementById('fruit-home').style.display = 'none';

}
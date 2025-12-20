package org.back.controller;

import org.back.entity.LogisticsAdmin;
import org.back.service.LogisticsAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/logistics")
public class LogisticsController {

    @Autowired
    private LogisticsAdminService logisticsAdminService;

    /**
     * 显示物流管理页面
     */
    @GetMapping("/admin")
    public String showAdminPage(HttpSession session, org.springframework.ui.Model model) {
        LogisticsAdmin admin = (LogisticsAdmin) session.getAttribute("logisticsAdmin");
        if (admin != null) {
            model.addAttribute("admin", admin);
        }
        return "logistics-management";
    }

    /**
     * 物流管理员登录
     */
    @PostMapping("/login")
    @ResponseBody
    public String login(@RequestParam String username, 
                       @RequestParam String password, 
                       HttpSession session) {
        try {
            LogisticsAdmin admin = logisticsAdminService.login(username, password);
            if (admin != null) {
                session.setAttribute("logisticsAdmin", admin);
                return "success";
            } else {
                return "用户名或密码错误";
            }
        } catch (Exception e) {
            return "登录失败：" + e.getMessage();
        }
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    @ResponseBody
    public String logout(HttpSession session) {
        session.removeAttribute("logisticsAdmin");
        return "success";
    }

    /**
     * 获取当前登录的管理员信息
     */
    @GetMapping("/current")
    @ResponseBody
    public LogisticsAdmin getCurrentAdmin(HttpSession session) {
        return (LogisticsAdmin) session.getAttribute("logisticsAdmin");
    }

    /**
     * 物流管理员注册
     */
    @PostMapping("/register")
    @ResponseBody
    public String register(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam String realName,
                          @RequestParam String phone,
                          @RequestParam String email) {
        try {
            // 检查用户名是否已存在
            LogisticsAdmin existingAdmin = logisticsAdminService.findByUsername(username);
            if (existingAdmin != null) {
                return "用户名已存在";
            }

            // 创建新的管理员对象
            LogisticsAdmin newAdmin = new LogisticsAdmin();
            newAdmin.setUsername(username);
            newAdmin.setPassword(password); // 密码会在service层进行MD5加密
            newAdmin.setRealName(realName);
            newAdmin.setPhone(phone);
            newAdmin.setEmail(email);

            // 调用服务层创建管理员
            boolean success = logisticsAdminService.createAdmin(newAdmin);
            if (success) {
                return "success";
            } else {
                return "注册失败，请稍后重试";
            }
        } catch (Exception e) {
            return "注册失败：" + e.getMessage();
        }
    }
}
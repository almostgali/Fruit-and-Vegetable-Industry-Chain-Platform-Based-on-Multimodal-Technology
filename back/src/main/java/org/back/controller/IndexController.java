package org.back.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class IndexController {

    // 根路径重定向到登录页面
    @GetMapping("/")
    public String root() {
        return "redirect:/user/login";
    }

    // 首页
    @GetMapping("/index")
    public String index(HttpSession session) {
        // 检查用户是否已登录
        if (session.getAttribute("user") == null) {
            return "redirect:/user/login";
        }
        return "index";
    }

    // 果蔬成熟度检测
    @GetMapping("/maturity-detection")
    public String maturityDetection(HttpSession session) {
        // 检查用户是否已登录
        if (session.getAttribute("user") == null) {
            return "redirect:/user/login";
        }
        return "maturity-detection";
    }

    // 果蔬品质检测
    @GetMapping("/quality-detection")
    public String qualityDetection(HttpSession session) {
        // 检查用户是否已登录
        if (session.getAttribute("user") == null) {
            return "redirect:/user/login";
        }
        return "quality-detection";
    }


}
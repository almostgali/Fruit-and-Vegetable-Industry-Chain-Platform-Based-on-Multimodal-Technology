package org.back.controller;

import org.back.entity.User;
import org.back.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 跳转到登录页面
    @GetMapping("/login")
    public String toLogin() {
        return "login";
    }

    // 登录请求
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
                        Model model, HttpSession session) {
        User user = userService.login(username, password);
        if (user != null) {
            session.setAttribute("user", user);
            return "redirect:/index";
        } else {
            model.addAttribute("error", "用户名或密码错误");
            return "login";
        }
    }

    // 跳转到注册页面
    @GetMapping("/register")
    public String toRegister() {
        return "register";
    }

    // 注册请求
    @PostMapping("/register")
    public String register(User user, Model model) {
        if (userService.checkUsernameExists(user.getUsername())) {
            model.addAttribute("error", "用户名已存在");
            return "register";
        }

        if (userService.register(user)) {
            // 注册成功，跳转到登录页面
            model.addAttribute("success", "注册成功，请登录");
            return "login";
        } else {
            // 注册失败
            model.addAttribute("error", "注册失败，请重试");
            return "register";
        }
    }

    // 退出登录
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/user/login";
    }
}
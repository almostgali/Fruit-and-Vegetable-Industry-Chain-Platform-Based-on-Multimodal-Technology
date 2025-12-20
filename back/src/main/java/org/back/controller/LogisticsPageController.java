package org.back.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 物流管理页面控制器
 * 处理物流管理相关的页面请求
 */
@Controller
@RequestMapping("/logistics")
public class LogisticsPageController {

    /**
     * 物流管理主页面
     * @return 物流管理页面模板
     */
    @GetMapping("/management")
    public String managementPage() {
        return "logistics-management";
    }

    /**
     * 物流管理首页（重定向到管理页面）
     * @return 重定向到管理页面
     */
    @GetMapping("")
    public String index() {
        return "redirect:/logistics/management";
    }
}
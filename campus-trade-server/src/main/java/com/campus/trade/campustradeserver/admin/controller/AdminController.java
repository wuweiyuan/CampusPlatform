package com.campus.trade.campustradeserver.admin.controller;

import com.campus.trade.campustradeserver.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @GetMapping("/ping")
    public ApiResponse<String> ping(){
        return new ApiResponse<>(0,"管理员访问成功","pong");
    }
}

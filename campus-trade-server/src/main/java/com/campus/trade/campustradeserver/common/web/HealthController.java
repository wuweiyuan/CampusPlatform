package com.campus.trade.campustradeserver.common.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.campus.trade.campustradeserver.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "服务健康")
@RestController
@RequestMapping("/api")
public class HealthController {
    @Operation(summary = "检查后端存活", description = "无需登录；data.status 固定返回 UP，表示应用能响应，不代表数据库、Redis 或邮件服务可用。")
    @GetMapping("/health")
    public ApiResponse<Map<String,String>> health(){
        return ApiResponse.success(Map.of("status","UP"));
    }
}

package com.campus.trade.campustradeserver.common.web;

import com.campus.trade.campustradeserver.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {
    @GetMapping("/health")
    public ApiResponse<Map<String,String>> health(){
        return ApiResponse.success(Map.of("status","UP"));
    }
}

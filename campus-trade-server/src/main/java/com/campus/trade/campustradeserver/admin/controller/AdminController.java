package com.campus.trade.campustradeserver.admin.controller;


import com.campus.trade.campustradeserver.admin.dto.AdminProductQuery;
import com.campus.trade.campustradeserver.admin.dto.AdminUserQuery;
import com.campus.trade.campustradeserver.admin.dto.UserStatusUpdateRequest;
import com.campus.trade.campustradeserver.admin.service.AdminProductService;
import com.campus.trade.campustradeserver.admin.service.AdminUserService;

import com.campus.trade.campustradeserver.admin.vo.AdminProductResponse;
import com.campus.trade.campustradeserver.admin.vo.AdminUserResponse;
import com.campus.trade.campustradeserver.auth.security.AuthenticatedUser;
import com.campus.trade.campustradeserver.common.api.ApiResponse;
import com.campus.trade.campustradeserver.common.api.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminUserService adminUserService;
    private final AdminProductService adminProductService;
    @GetMapping("/users")
    public ApiResponse<PageResponse<AdminUserResponse>> listUsers(
            @Valid AdminUserQuery query
            ){
        return ApiResponse.success(adminUserService.listUsers(query));
    }

    @PatchMapping("/users/{id}/status")
    public ApiResponse<Void> updateUserStatus(
            @PathVariable @Positive(message = "用户ID必须为正整数") Long id,
            @Valid @RequestBody UserStatusUpdateRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ){
        adminUserService.updateUserStatus(currentUser.id(), id ,request.getStatus());
        return new ApiResponse<>(0,"用户状态更新成功",null);
    }

    @GetMapping("/products")
    public ApiResponse<PageResponse<AdminProductResponse>> listProducts(
            @Valid AdminProductQuery query
    ){
        return ApiResponse.success(adminProductService.listProducts(query));
    }

    @PatchMapping("/products/{id}/off-shelf")
    public ApiResponse<Void> offShelfProduct(
            @PathVariable @Positive(message = "商品ID必须为正整数") Long id
    ){
        adminProductService.offShelfProduct(id);
        return new ApiResponse<>(0,"商品已下架",null);
    }
    @GetMapping("/ping")
    public ApiResponse<String> ping(){
        return new ApiResponse<>(0,"管理员访问成功","pong");
    }
}

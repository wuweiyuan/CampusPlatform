package com.campus.trade.campustradeserver.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import org.springdoc.core.annotations.ParameterObject;

import com.campus.trade.campustradeserver.admin.dto.AdminOrderQuery;
import com.campus.trade.campustradeserver.admin.dto.AdminProductQuery;
import com.campus.trade.campustradeserver.admin.dto.AdminUserQuery;
import com.campus.trade.campustradeserver.admin.dto.UserStatusUpdateRequest;
import com.campus.trade.campustradeserver.admin.service.AdminOrderService;
import com.campus.trade.campustradeserver.admin.service.AdminProductService;
import com.campus.trade.campustradeserver.admin.service.AdminUserService;

import com.campus.trade.campustradeserver.admin.vo.AdminProductResponse;
import com.campus.trade.campustradeserver.admin.vo.AdminUserResponse;
import com.campus.trade.campustradeserver.auth.security.AuthenticatedUser;
import com.campus.trade.campustradeserver.common.api.ApiResponse;
import com.campus.trade.campustradeserver.common.api.PageResponse;
import com.campus.trade.campustradeserver.order.vo.OrderPageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员：平台管理")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminUserService adminUserService;
    private final AdminProductService adminProductService;
    private final AdminOrderService adminOrderService;
    @Operation(summary = "分页查询用户", description = "仅 ADMIN；支持用户名、邮箱、角色和状态筛选，不返回密码。", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/users")
    public ApiResponse<PageResponse<AdminUserResponse>> listUsers(
            @ParameterObject @Valid AdminUserQuery query
            ){
        return ApiResponse.success(adminUserService.listUsers(query));
    }

    @Operation(summary = "启用或禁用用户", description = "仅 ADMIN；请求体 status 为数字 1（启用）或 0（禁用），不能禁用当前登录管理员自身。", security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping("/users/{id}/status")
    public ApiResponse<Void> updateUserStatus(
            @Parameter(description = "用户 ID，正整数", example = "1", required = true) @PathVariable @Positive(message = "用户ID必须为正整数") Long id,
            @Valid @RequestBody UserStatusUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser currentUser
    ){
        adminUserService.updateUserStatus(currentUser.id(), id ,request.getStatus());
        return new ApiResponse<>(0,"用户状态更新成功",null);
    }

    @Operation(summary = "分页查询全平台商品", description = "仅 ADMIN；支持卖家、分类、状态和关键词筛选。", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/products")
    public ApiResponse<PageResponse<AdminProductResponse>> listProducts(
            @ParameterObject @Valid AdminProductQuery query
    ){
        return ApiResponse.success(adminProductService.listProducts(query));
    }

    @Operation(summary = "管理员下架商品", description = "仅 ADMIN；无需是卖家，但仅允许下架 ON_SALE 商品。", security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping("/products/{id}/off-shelf")
    public ApiResponse<Void> offShelfProduct(
            @Parameter(description = "商品 ID，正整数", example = "1", required = true) @PathVariable @Positive(message = "商品ID必须为正整数") Long id
    ){
        adminProductService.offShelfProduct(id);
        return new ApiResponse<>(0,"商品已下架",null);
    }

    @Operation(summary = "分页查询全平台订单", description = "仅 ADMIN；支持订单号、状态、买家和卖家筛选。", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/orders")
    public ApiResponse<PageResponse<OrderPageResponse>> listOrders(@ParameterObject @Valid AdminOrderQuery query){
        return ApiResponse.success(adminOrderService.listOrders(query));
    }

    @Operation(summary = "检查管理员访问权限", description = "仅 ADMIN；用于确认当前 Token 具有管理员权限。", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/ping")
    public ApiResponse<String> ping(){
        return new ApiResponse<>(0,"管理员访问成功","pong");
    }
}

package com.campus.trade.campustradeserver.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import org.springdoc.core.annotations.ParameterObject;
import com.campus.trade.campustradeserver.auth.security.AuthenticatedUser;
import com.campus.trade.campustradeserver.common.api.ApiResponse;
import com.campus.trade.campustradeserver.common.api.PageResponse;
import com.campus.trade.campustradeserver.common.exception.BusinessException;
import com.campus.trade.campustradeserver.order.dto.CreateOrderRequest;
import com.campus.trade.campustradeserver.order.dto.OrderQuery;
import com.campus.trade.campustradeserver.order.service.OrderService;
import com.campus.trade.campustradeserver.order.vo.OrderDetailResponse;
import com.campus.trade.campustradeserver.order.vo.OrderPageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "交易订单")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @Operation(summary = "创建订单", description = "需要登录；不能购买自己的商品。按当前商品价格下单，订单为 PENDING_PAYMENT，商品由 ON_SALE 锁定为 LOCKED。", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ApiResponse<OrderDetailResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser currentUser
            ){
        OrderDetailResponse response= orderService.createOrder(currentUser.id(), request.getProductId());
        return new ApiResponse<>(0,"下单成功",response);
    }

    @Operation(summary = "取消待付款订单", description = "仅订单买家；订单由 PENDING_PAYMENT 变为 CANCELLED，关联商品由 LOCKED 恢复为 ON_SALE。", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancelOrder(
            @Parameter(description = "订单 ID，正整数", example = "1", required = true) @PathVariable Long orderId,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser currentUser
            ){
        validateOrderId(orderId);
        orderService.cancelOrder(currentUser.id(), orderId);
        return new ApiResponse<>(0,"订单已取消",null);
    }

    @Operation(summary = "模拟付款", description = "仅订单买家；不接入真实支付机构、不产生真实扣款。订单由 PENDING_PAYMENT 变为 PAID，商品由 LOCKED 变为 SOLD。", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{orderId}/pay")
    public ApiResponse<Void> payOrder(
            @Parameter(description = "订单 ID，正整数", example = "1", required = true) @PathVariable Long orderId,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser currentUser
    ){
        validateOrderId(orderId);
        orderService.payOrder(currentUser.id(), orderId);
        return new ApiResponse<>(0,"付款成功",null);
    }

    @Operation(summary = "确认交易完成", description = "仅订单买家；将 PAID 订单改为 COMPLETED，商品保持 SOLD。", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{orderId}/complete")
    public ApiResponse<Void> completeOrder(
            @Parameter(description = "订单 ID，正整数", example = "1", required = true) @PathVariable Long orderId,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser currentUser
    ){
        validateOrderId(orderId);
        orderService.completeOrder(currentUser.id(), orderId);
        return new ApiResponse<>(0,"订单已确认完成",null);
    }

    @Operation(summary = "查看订单详情", description = "需要登录且为该订单买家或卖家；不能查看他人的订单。", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailResponse> getOrderDetail(
            @Parameter(description = "订单 ID，正整数", example = "1", required = true) @PathVariable Long orderId,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser currentUser
    ){
        validateOrderId(orderId);
        OrderDetailResponse response = orderService.getOrderDetail(currentUser.id(), orderId);
        return new ApiResponse<>(0,"查询订单详情完成",response);
    }

    @Operation(summary = "分页查询我买入的订单", description = "需要登录；仅查询当前用户作为买家的订单。", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/buying")
    public ApiResponse<PageResponse<OrderPageResponse>> listBuyingOrders (
            @ParameterObject @Valid OrderQuery query,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser currentUser
            ){
        PageResponse<OrderPageResponse> response = orderService.listBuyingOrders(currentUser.id(), query);
        return new ApiResponse<>(0,"查询买入订单成功",response);
    }

    @Operation(summary = "分页查询我卖出的订单", description = "需要登录；仅查询当前用户作为卖家的订单。", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/selling")
    public ApiResponse<PageResponse<OrderPageResponse>> listSellingOrders (
            @ParameterObject @Valid OrderQuery query,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser currentUser
    ){
        PageResponse<OrderPageResponse> response = orderService.listSellingOrders(currentUser.id(), query);
        return new ApiResponse<>(0,"查询卖出订单成功",response);
    }

    private void validateOrderId(Long orderId){
        if(orderId == null || orderId <=0){
            throw new BusinessException(400,"订单ID必须为正整数");
        }
    }
}

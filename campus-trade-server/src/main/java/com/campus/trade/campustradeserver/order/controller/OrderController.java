package com.campus.trade.campustradeserver.order.controller;

import com.campus.trade.campustradeserver.auth.security.AuthenticatedUser;
import com.campus.trade.campustradeserver.common.api.ApiResponse;
import com.campus.trade.campustradeserver.common.exception.BusinessException;
import com.campus.trade.campustradeserver.order.dto.CreateOrderRequest;
import com.campus.trade.campustradeserver.order.service.OrderService;
import com.campus.trade.campustradeserver.order.vo.OrderDetailResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ApiResponse<OrderDetailResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
            ){
        OrderDetailResponse response= orderService.createOrder(currentUser.id(), request.getProductId());
        return new ApiResponse<>(0,"下单成功",response);
    }

    @PostMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal AuthenticatedUser currentUser
            ){
        validateOrderId(orderId);
        orderService.cancelOrder(currentUser.id(), orderId);
        return new ApiResponse<>(0,"订单已取消",null);
    }

    @PostMapping("/{orderId}/pay")
    public ApiResponse<Void> payOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ){
        validateOrderId(orderId);
        orderService.payOrder(currentUser.id(), orderId);
        return new ApiResponse<>(0,"付款成功",null);
    }

    private void validateOrderId(Long orderId){
        if(orderId == null || orderId <=0){
            throw new BusinessException(400,"订单ID必须为正整数");
        }
    }
}

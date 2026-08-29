package com.campus.trade.campustradeserver.order.controller;

import com.campus.trade.campustradeserver.auth.security.AuthenticatedUser;
import com.campus.trade.campustradeserver.common.api.ApiResponse;
import com.campus.trade.campustradeserver.order.dto.CreateOrderRequest;
import com.campus.trade.campustradeserver.order.service.OrderService;
import com.campus.trade.campustradeserver.order.vo.OrderDetailResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

package com.campus.trade.campustradeserver.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.campustradeserver.common.api.PageResponse;
import com.campus.trade.campustradeserver.common.exception.BusinessException;
import com.campus.trade.campustradeserver.order.dto.OrderQuery;
import com.campus.trade.campustradeserver.order.entity.Order;
import com.campus.trade.campustradeserver.order.enums.OrderStatus;
import com.campus.trade.campustradeserver.order.mapper.OrderMapper;
import com.campus.trade.campustradeserver.order.vo.OrderDetailResponse;
import com.campus.trade.campustradeserver.order.vo.OrderPageResponse;
import com.campus.trade.campustradeserver.product.entity.Product;
import com.campus.trade.campustradeserver.product.enums.ProductStatus;
import com.campus.trade.campustradeserver.product.mapper.ProductMapper;
import com.campus.trade.campustradeserver.product.service.HotProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final DateTimeFormatter ORDER_NO_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final int ORDER_NO_RETRY_LIMIT = 3;


    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final HotProductService hotProductService;

    @Transactional
    public OrderDetailResponse createOrder(Long buyerId, Long productId){
        Product product = productMapper.selectById(productId);
        if(product == null){
            throw new BusinessException(3001,"商品不存在或不可交易");
        }
        if(product.getSellerId().equals(buyerId)){
            throw new BusinessException(4003,"不能购买自己的商品");
        }
        int updatedRows = productMapper.updateStatusIfCurrentStatus(productId, ProductStatus.ON_SALE.name(),ProductStatus.LOCKED.name());
        if(updatedRows != 1){
            throw new BusinessException(3003,"当前商品状态不允许下单");
        }
        Product lockedProduct = productMapper.selectById(productId);
        Order order = new Order();
        order.setAmount(lockedProduct.getPrice());
        order.setBuyerId(buyerId);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setProductId(lockedProduct.getId());
        order.setSellerId(lockedProduct.getSellerId());
        insertOrderWithRetry(order);
        OrderDetailResponse response = orderMapper.selectOrderDetailById(order.getId());
        if(response == null){
            throw new BusinessException(400,"订单创建后查询失败");
        }
        hotProductService.evictHotProductCacheAfterCommit();
        return response;
    }

    @Transactional
    public void cancelOrder(Long buyerId,Long orderId){
        Order order = orderMapper.selectById(orderId);
        if(order == null){
            throw new BusinessException(4001,"订单不存在");
        }
        if(!order.getBuyerId().equals(buyerId)){
            throw new AccessDeniedException("无权取消该订单");
        }
        if(order.getStatus() != OrderStatus.PENDING_PAYMENT){
            throw new BusinessException(4002,"当前订单状态不允许取消");
        }
        int updatedOrderRows = orderMapper.updateStatusIfCurrentStatus(
                orderId,
                OrderStatus.PENDING_PAYMENT.getCode(),
                OrderStatus.CANCELLED.getCode()
        );
        if(updatedOrderRows !=1){
            throw new BusinessException(4002,"当前订单状态不允许取消");
        }
        int updatedProductRows = productMapper.updateStatusIfCurrentStatus(
                order.getProductId(),
                ProductStatus.LOCKED.name(),
                ProductStatus.ON_SALE.name()
        );
        if (updatedProductRows != 1) {
            throw new BusinessException(4002, "订单关联商品状态异常，取消失败");
        }
        hotProductService.evictHotProductCacheAfterCommit();
    }


    @Transactional
    public void payOrder(Long buyerId,Long orderId){
        Order order = orderMapper.selectById(orderId);
        if(order == null){
            throw new BusinessException(4001,"订单不存在");
        }

        if(!order.getBuyerId().equals(buyerId)){
            throw new AccessDeniedException("无权支付该订单");
        }

        if(order.getStatus()!= OrderStatus.PENDING_PAYMENT){
            throw new BusinessException(4002,"当前订单状态不允许付款");
        }
        int updatedOrderRows = orderMapper.updateStatusAndPaidAtIfCurrentStatus(orderId,OrderStatus.PENDING_PAYMENT.getCode(), OrderStatus.PAID.getCode());
        if(updatedOrderRows != 1){
            throw new BusinessException(4002, "当前订单状态不允许付款");
        }

        int updatedProductRows = productMapper.updateStatusIfCurrentStatus(order.getProductId(),ProductStatus.LOCKED.name(), ProductStatus.SOLD.name());
        if(updatedProductRows != 1){
            throw new BusinessException(4002,"订单关联商品状态异常，付款失败");
        }
        hotProductService.evictHotProductCacheAfterCommit();
    }

    @Transactional
    public void completeOrder(Long buyerId,Long orderId){
        Order order = orderMapper.selectById(orderId);
        if(order == null){
            throw new BusinessException(4001,"订单不存在");
        }
        if(!order.getBuyerId().equals(buyerId)){
            throw new AccessDeniedException("无权确认该订单");
        }
        if(order.getStatus() != OrderStatus.PAID){
            throw new BusinessException(4002,"当前订单状态不允许确认完成");
        }
        int updatedRow = orderMapper.updateStatusAndCompletedAtIfCurrentStatus(orderId,OrderStatus.PAID.getCode(),OrderStatus.COMPLETED.getCode());
        if(updatedRow != 1){
            throw new BusinessException(4002,"当前订单状态不允许确认完成");
        }
    }

    public OrderDetailResponse getOrderDetail (Long currentUserId, Long orderId){
        Order order = orderMapper.selectById(orderId);
        if(order == null){
            throw new BusinessException(4001,"订单不存在");
        }
        boolean isBuyer = order.getBuyerId().equals(currentUserId);
        boolean isSeller = order.getSellerId().equals(currentUserId);
        if(!isBuyer && !isSeller){
            throw new AccessDeniedException("无权查看该订单");
        }
        OrderDetailResponse response = orderMapper.selectOrderDetailById(orderId);
        if (response == null) {
            throw new BusinessException(4001, "订单详情不存在");
        }
        return response;
    }

    public PageResponse<OrderPageResponse> listBuyingOrders(
            Long buyerId,
            OrderQuery query
    ) {
        Page<OrderPageResponse> page = new Page<>(
                query.getPage(),
                query.getPageSize()
        );

        IPage<OrderPageResponse> result = orderMapper.selectBuyingOrderPage(
                page,
                buyerId
        );

        return toPageResponse(result);
    }

    public PageResponse<OrderPageResponse> listSellingOrders(
            Long sellerId,
            OrderQuery query
    ) {
        Page<OrderPageResponse> page = new Page<>(
                query.getPage(),
                query.getPageSize()
        );

        IPage<OrderPageResponse> result = orderMapper.selectSellingOrderPage(
                page,
                sellerId
        );

        return toPageResponse(result);
    }

    private PageResponse<OrderPageResponse> toPageResponse(
            IPage<OrderPageResponse> result
    ) {
        PageResponse<OrderPageResponse> response = new PageResponse<>();
        response.setPage(Math.toIntExact(result.getCurrent()));
        response.setPageSize(Math.toIntExact(result.getSize()));
        response.setTotal(result.getTotal());
        response.setRecords(result.getRecords());
        return response;
    }

    private void insertOrderWithRetry(Order order){
        for (int attempt = 0; attempt < ORDER_NO_RETRY_LIMIT; attempt++){
            order.setOrderNo(generateOrderNo());
            try {
                if(orderMapper.insert(order) == 1){
                    return;
                }
                throw new BusinessException(400,"创建订单失败");
            }catch (DuplicateKeyException exception){
                order.setId(null);
            }
        }
        throw new BusinessException(400,"订单号生成失败，请重试");
    }

    private String generateOrderNo(){
        String timePart = LocalDateTime.now().format(ORDER_NO_TIME_FORMATTER);

        int randomNumber = ThreadLocalRandom.current().nextInt(1_000_000);
        String randomPart = String.format("%06d",randomNumber);
        return timePart + randomPart;

    }


}

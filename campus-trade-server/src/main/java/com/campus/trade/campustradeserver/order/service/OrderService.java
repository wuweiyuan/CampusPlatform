package com.campus.trade.campustradeserver.order.service;

import com.campus.trade.campustradeserver.common.exception.BusinessException;
import com.campus.trade.campustradeserver.order.entity.Order;
import com.campus.trade.campustradeserver.order.enums.OrderStatus;
import com.campus.trade.campustradeserver.order.mapper.OrderMapper;
import com.campus.trade.campustradeserver.order.vo.OrderDetailResponse;
import com.campus.trade.campustradeserver.product.entity.Product;
import com.campus.trade.campustradeserver.product.enums.ProductStatus;
import com.campus.trade.campustradeserver.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
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

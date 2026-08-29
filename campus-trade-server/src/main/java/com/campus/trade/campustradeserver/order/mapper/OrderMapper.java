package com.campus.trade.campustradeserver.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trade.campustradeserver.order.entity.Order;
import com.campus.trade.campustradeserver.order.vo.OrderDetailResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("""
        select 
            o.id,
                          o.order_no AS orderNo,
                          o.buyer_id AS buyerId,
                          buyer.username AS buyerName,
                          o.seller_id AS sellerId,
                          seller.username AS sellerName,
                          o.product_id AS productId,
                          p.title AS productTitle,
                          p.image_base64 AS productImageBase64,
                          o.amount,
                          o.status,
                          o.created_at AS createdAt,
                          o.paid_at AS paidAt,
                          o.completed_at AS completedAt,
                          o.updated_at AS updatedAt
            from orders o
            INNER JOIN sys_user buyer ON buyer.id = o.buyer_id
            INNER JOIN sys_user seller ON seller.id = o.seller_id
                 INNER JOIN product p ON p.id = o.product_id
                 where o.id = #{orderId}
    """)
    OrderDetailResponse selectOrderDetailById(@Param("orderId") Long orderId);
}

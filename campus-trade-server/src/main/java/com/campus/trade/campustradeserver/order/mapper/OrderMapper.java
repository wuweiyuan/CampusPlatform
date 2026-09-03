package com.campus.trade.campustradeserver.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.campustradeserver.order.entity.Order;
import com.campus.trade.campustradeserver.order.vo.OrderDetailResponse;
import com.campus.trade.campustradeserver.order.vo.OrderPageResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;



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


    @Update("""
        UPDATE orders SET status = #{targetStatus}
        WHERE id = #{orderId}
        AND status = #{expectedStatus}
    """)
    int updateStatusIfCurrentStatus(
            @Param("orderId") Long orderId,
            @Param("expectedStatus") String expectedStatus,
            @Param("targetStatus") String targetStatus
    );

    @Update("""
    UPDATE orders
    SET status = #{targetStatus},
    paid_at = NOW()
    WHERE id = #{orderId}
    AND status = #{expectedStatus}
""")
    int updateStatusAndPaidAtIfCurrentStatus(
            @Param("orderId") Long orderId,
            @Param("expectedStatus") String expectedStatus,
                    @Param("targetStatus") String targetStatus
    );

    @Update("""
        UPDATE orders
        SET status = #{targetStatus},
            completed_at = NOW()
        WHERE id = #{orderId} 
        AND status = #{expectedStatus}
    """)
    int updateStatusAndCompletedAtIfCurrentStatus(
            @Param("orderId") Long orderId,
            @Param("expectedStatus") String expectedStatus,
            @Param("targetStatus") String targetStatus
    );


    @Select("""
      SELECT
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
      FROM orders o
      INNER JOIN sys_user buyer ON buyer.id = o.buyer_id
      INNER JOIN sys_user seller ON seller.id = o.seller_id
      INNER JOIN product p ON p.id = o.product_id
      WHERE o.buyer_id = #{buyerId}
      ORDER BY o.created_at DESC, o.id DESC
  """)
    IPage<OrderPageResponse> selectBuyingOrderPage(
            Page<OrderPageResponse> page,
            @Param("buyerId") Long buyerId
    );

    @Select("""
      SELECT
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
      FROM orders o
      INNER JOIN sys_user buyer ON buyer.id = o.buyer_id
      INNER JOIN sys_user seller ON seller.id = o.seller_id
      INNER JOIN product p ON p.id = o.product_id
      WHERE o.seller_id = #{sellerId}
      ORDER BY o.created_at DESC, o.id DESC
  """)
    IPage<OrderPageResponse> selectSellingOrderPage(
            Page<OrderPageResponse> page,
            @Param("sellerId") Long sellerId
    );

    @Select("""
      <script>
      SELECT
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
      FROM orders o
      INNER JOIN sys_user buyer ON buyer.id = o.buyer_id
      INNER JOIN sys_user seller ON seller.id = o.seller_id
      INNER JOIN product p ON p.id = o.product_id
      WHERE 1 = 1
      <if test="orderNo != null and orderNo != ''">
          AND o.order_no = #{orderNo}
      </if>
      <if test="status != null and status != ''">
          AND o.status = #{status}
      </if>
      <if test="buyerId != null">
          AND o.buyer_id = #{buyerId}
      </if>
      <if test="sellerId != null">
          AND o.seller_id = #{sellerId}
      </if>
      ORDER BY o.created_at DESC, o.id DESC
      </script>
      """)
    IPage<OrderPageResponse> selectAdminOrderPage(
            Page<OrderPageResponse> page,
            @Param("orderNo") String orderNo,
            @Param("status") String status,
            @Param("buyerId") Long buyerId,
            @Param("sellerId") Long sellerId
    );
}

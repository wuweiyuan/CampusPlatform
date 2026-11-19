package com.campus.trade.campustradeserver.order.mapper;

import com.campus.trade.campustradeserver.order.entity.OrderTimeoutOutbox;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderTimeoutOutboxMapper {
    @Insert("""
        INSERT INTO order_timeout_outbox (order_id, due_at)
        VALUES (#{orderId},TIMESTAMPADD(SECOND,#{timeoutSeconds},CURRENT_TIMESTAMP))
    """)
    int insertPending(
            @Param("orderId") Long orderId,
            @Param("timeoutSeconds") long timeoutSeconds
    );

    @Select("""
    SELECT  id,
    order_id AS orderId,
    due_at AS dueAt,
    status,
    attempts,
    created_at AS createdAt,
    sent_at AS sentAt
    FROM order_timeout_outbox
    WHERE status = 'PENDING'
    AND due_at <= CURRENT_TIMESTAMP
    ORDER BY due_at, id
    LIMIT #{limit}
    """)
    List<OrderTimeoutOutbox> selectDuePending(@Param("limit") int limit);

    @Update("""
    UPDATE order_timeout_outbox
    SET status = 'SENT',
    sent_at = CURRENT_TIMESTAMP
    WHERE id = #{id}
    AND status = 'PENDING'
""")
    int markSent(@Param("id") Long id);

    @Update("""
    UPDATE order_timeout_outbox
    SET attempts = attempts + 1
    WHERE id = #{id}
      AND status = 'PENDING'
    """)
    int incrementAttempts(@Param("id") Long id);
}

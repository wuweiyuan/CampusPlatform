package com.campus.trade.campustradeserver.order.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
}

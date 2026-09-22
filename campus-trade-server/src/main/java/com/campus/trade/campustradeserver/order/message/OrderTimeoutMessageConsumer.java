package com.campus.trade.campustradeserver.order.message;
import com.campus.trade.campustradeserver.common.config.RabbitMqConfig;
import com.campus.trade.campustradeserver.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderTimeoutMessageConsumer {
    private final OrderService orderService;

    @RabbitListener(queues = RabbitMqConfig.ORDER_TIMEOUT_QUEUE)
    public void consume(OrderTimeoutMessage message){
        if (message == null || message.orderId() == null){
            throw new IllegalArgumentException("订单超时消息缺少订单ID");
        }
        orderService.cancelExpiredOrder(message.orderId());
    }
}

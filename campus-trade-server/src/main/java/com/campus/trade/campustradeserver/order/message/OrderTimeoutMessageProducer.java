package com.campus.trade.campustradeserver.order.message;

import com.campus.trade.campustradeserver.common.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderTimeoutMessageProducer {
    private static final long CONFIRM_TIMEOUT_MILLIS = 5000;
    private final RabbitTemplate rabbitTemplate;

    public void send(Long orderId){
        OrderTimeoutMessage message = new OrderTimeoutMessage(orderId);
        rabbitTemplate.invoke(operations -> {
                    operations.convertAndSend(
                            RabbitMqConfig.ORDER_DELAY_EXCHANGE,
                            RabbitMqConfig.ORDER_DELAY_ROUTING_KEY,
                            message
                    );
                    operations.waitForConfirmsOrDie(CONFIRM_TIMEOUT_MILLIS);
                    return null;
                }
        );

    }
}

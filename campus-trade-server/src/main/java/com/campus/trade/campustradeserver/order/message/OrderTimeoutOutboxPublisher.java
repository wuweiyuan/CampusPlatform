package com.campus.trade.campustradeserver.order.message;

import com.campus.trade.campustradeserver.order.entity.OrderTimeoutOutbox;
import com.campus.trade.campustradeserver.order.mapper.OrderTimeoutOutboxMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderTimeoutOutboxPublisher {

    private final OrderTimeoutOutboxMapper outboxMapper;
    private final OrderTimeoutMessageProducer messageProducer;
    @Scheduled(fixedDelayString = "${app.order.outbox-poll-interval-ms:1000}")
    public void publishDueMessages(){
        List<OrderTimeoutOutbox> events = outboxMapper.selectDuePending(100);
        for (OrderTimeoutOutbox event : events){
            try {
                int updated = outboxMapper.incrementAttempts(event.getId());
                if (updated != 1){
                    continue;
                }
                messageProducer.sendTimeout(event.getOrderId());

                int marked = outboxMapper.markSent(event.getId());
                if(marked != 1){
                    log.warn("消息已发布，但本次未更新发送状态，outboxId={}",event.getId());
                }
            }catch (Exception exception){
                log.warn("订单超时消息处理失败，outboxId={}, orderId={}",event.getId(), event.getOrderId(), exception);
            }
        }
    }
}

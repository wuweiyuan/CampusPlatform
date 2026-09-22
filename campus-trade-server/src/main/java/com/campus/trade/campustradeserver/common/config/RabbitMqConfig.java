package com.campus.trade.campustradeserver.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RabbitMqConfig {
    public static final String ORDER_DELAY_EXCHANGE =
            "order.delay.exchange";

    public static final String ORDER_DELAY_QUEUE =
            "order.delay.queue";

    public static final String ORDER_DELAY_ROUTING_KEY =
            "order.delay";

    public static final String ORDER_TIMEOUT_EXCHANGE =
            "order.timeout.exchange";

    public static final String ORDER_TIMEOUT_QUEUE =
            "order.timeout.queue";

    public static final String ORDER_TIMEOUT_ROUTING_KEY =
            "order.timeout";

    public static final String ORDER_FAILED_EXCHANGE =
            "order.failed.exchange";

    public static final String ORDER_FAILED_QUEUE =
            "order.failed.queue";

    public static final String ORDER_FAILED_ROUTING_KEY =
            "order.failed";

//    创建延迟交换机
    @Bean
    public DirectExchange orderDelayExchange(){
        return new DirectExchange(
            ORDER_DELAY_EXCHANGE,
            true,
            false
        );
    }

//    创建超时交换机
    @Bean
    public DirectExchange orderTimeoutExchange(){
        return new DirectExchange(
                ORDER_TIMEOUT_EXCHANGE,
                true,
                false
        );
    }

//    创建失败交换机
    @Bean
    public DirectExchange orderFailedExchange(){
        return new DirectExchange(
                ORDER_FAILED_EXCHANGE,
                true,
                false
        );
    }

//    创建延迟队列
    @Bean
    public Queue orderDelayQueue(@Value("${app.order.payment-timeout}") Duration paymentTimeout){
        return  QueueBuilder
                .durable(ORDER_DELAY_QUEUE)
                .ttl(Math.toIntExact(paymentTimeout.toMillis()))
                .deadLetterExchange(ORDER_TIMEOUT_EXCHANGE)
                .deadLetterRoutingKey(ORDER_TIMEOUT_ROUTING_KEY)
                .build();
    }

    //创建超时处理队列
    @Bean
    public Queue orderTimeoutQueue() {
        return QueueBuilder
                .durable(ORDER_TIMEOUT_QUEUE)
                .deadLetterExchange(ORDER_FAILED_EXCHANGE)
                .deadLetterRoutingKey(ORDER_FAILED_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue orderFailedQueue() {
        return QueueBuilder
                .durable(ORDER_FAILED_QUEUE)
                .build();
    }

    @Bean
    public Binding orderDelayBinding(
            @Qualifier("orderDelayQueue") Queue queue,
            @Qualifier("orderDelayExchange") DirectExchange exchange
    ) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(ORDER_DELAY_ROUTING_KEY);
    }

    @Bean
    public Binding orderTimeoutBinding(
            @Qualifier("orderTimeoutQueue") Queue queue,
            @Qualifier("orderTimeoutExchange") DirectExchange exchange
    ) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(ORDER_TIMEOUT_ROUTING_KEY);
    }

    @Bean
    public Binding orderFailedBinding(
            @Qualifier("orderFailedQueue") Queue queue,
            @Qualifier("orderFailedExchange") DirectExchange exchange
    ) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(ORDER_FAILED_ROUTING_KEY);
    }

    @Bean
    public JacksonJsonMessageConverter rabbitMessageConverter(){
        return new JacksonJsonMessageConverter();
    }
}

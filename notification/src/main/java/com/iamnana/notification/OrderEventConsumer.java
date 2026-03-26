package com.iamnana.notification;

import com.iamnana.notification.payload.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;


@Service
@Slf4j
public class OrderEventConsumer {
    @Bean
    public Consumer<OrderCreatedEvent> orderCreated(){
        return event -> {
            log.info("Received order created event for order: {}", event.getOrderId());
            log.info("Received order created event for user id: {}", event.getUserId());
        };
    }
}

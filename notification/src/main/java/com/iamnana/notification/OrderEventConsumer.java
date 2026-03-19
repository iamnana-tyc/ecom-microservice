package com.iamnana.notification;

import com.iamnana.notification.payload.OrderCreatedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;


@Service
public class OrderEventConsumer {
    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void handleOrderEvent(OrderCreatedEvent orderCreatedEvent){
        System.out.println("Received Order Event: " + orderCreatedEvent);
    }
}

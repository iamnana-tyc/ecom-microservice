package com.ecommerce.order.service;


import com.ecommerce.order.dto.OrderItemDTO;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.model.CartItem;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    public Optional<OrderResponse> createOrder(String userId) {
//        // validate user
//        Optional<User> exitingUser = userRepository.findById(Long.valueOf(userId));
//        if (exitingUser.isEmpty()) {
//            // handle validation
//            return Optional.empty();
//        }
//        User user = exitingUser.get();

        // validate cart items
        List<CartItem> cartItems = cartService.getCart(userId);
        if (cartItems.isEmpty()) {
            return Optional.empty();
        }

        // calculate total price {get price for each item and add them all}
        BigDecimal totalPrice = cartItems.stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // create order
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(totalPrice);
        order.setStatus(OrderStatus.CONFIRMED);

        List<OrderItem> orderItems = cartItems.stream()
                .map(item -> new OrderItem(
                        null,
                        item.getProductId(),
                        item.getQuantity(),
                        item.getPrice(),
                        order
                ))
                .toList();

        order.setItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        // clear items in cart after order
        cartService.clearCart(userId);

        //public message on Rabbitmq
        rabbitTemplate.convertAndSend(exchangeName,
                routingKey,
                Map.of("orderId", savedOrder.getId(), "status", "CREATED"));


        return Optional.of(mapToOrderResponse(savedOrder));
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getItems().stream()
                        .map(orderItem -> new OrderItemDTO(
                                orderItem.getId(),
                                orderItem.getProductId(),
                                orderItem.getPrice(),
                                orderItem.getQuantity(),
                                orderItem.getPrice().multiply(new BigDecimal(orderItem.getQuantity()))
                        )).toList(),
                order.getCreatedAt()
        );
    }
}

package com.ecommerce.order.service;


import com.ecommerce.order.dto.OrderCreatedEvent;
import com.ecommerce.order.dto.OrderItemDTO;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.model.CartItem;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final StreamBridge streamBridge;

    public Optional<OrderResponse> createOrder(String userId) {
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
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getStatus(),
                mapToOrderItemDTOs(savedOrder.getItems()),
                savedOrder.getTotalAmount(),
                savedOrder.getCreatedAt()
        );

        streamBridge.send("createOrder-out-0", orderCreatedEvent);

        return Optional.of(mapToOrderResponse(savedOrder));
    }

    private List<OrderItemDTO> mapToOrderItemDTOs(List<OrderItem> items){
        return items.stream()
                .map(item -> new OrderItemDTO(
                        item.getId(),
                        item.getProductId(),
                        item.getPrice(),
                        item.getQuantity(),
                        item.getPrice().multiply(new BigDecimal(item.getQuantity()))
                ))
                .collect(Collectors.toList());
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

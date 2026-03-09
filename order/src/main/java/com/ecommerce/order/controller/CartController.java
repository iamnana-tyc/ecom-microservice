package com.ecommerce.order.controller;


import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.model.CartItem;
import com.ecommerce.order.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping
    public ResponseEntity<String> addToCart(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody CartItemRequest request
    ){
        if(!cartService.addToCart(userId, request)){
            return ResponseEntity.badRequest().body("Product is out of stock, or either user or product not found");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("Product added to cart");
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> deleteCartItem(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String productId
    ){
        boolean deleted = cartService.deleteProductFromCart(userId, productId);

        return deleted ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<CartItem>> getCart(
            @RequestHeader("X-User-Id") String userId){
        List<CartItem> cartItem = cartService.getCart(userId);

        return ResponseEntity.ok(cartItem);
    }
}

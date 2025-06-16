package com.shop.backend.controller;


import com.shop.backend.dto.CartItemRequest;
import com.shop.backend.dto.CartItemResponse;
import com.shop.backend.dto.CartResultResponse;
import com.shop.backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<List<CartItemResponse>> getCart(
            @AuthenticationPrincipal(expression = "id") Long buyerId) {
        return ResponseEntity.ok(cartService.getCart(buyerId));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(
            @AuthenticationPrincipal(expression = "id") Long buyerId,
            @RequestBody CartItemRequest request) {
        cartService.addToCart(buyerId, request);
        return ResponseEntity.status(201).body("Added to cart");
    }

    @DeleteMapping("/delItem/{itemId}")
    public ResponseEntity<?> deleteItem(
            @AuthenticationPrincipal(expression = "id") Long buyerId,
            @PathVariable Long itemId) {
        cartService.deleteCartItem(buyerId, itemId);
        return ResponseEntity.ok("Item removed");
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(@AuthenticationPrincipal(expression = "id") Long buyerId) {
        cartService.clearCart(buyerId);
        return ResponseEntity.ok("Cart cleared");
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<?> updateQuantity(@AuthenticationPrincipal(expression = "id") Long buyerId,
                                            @PathVariable Long itemId,
                                            @RequestBody CartItemRequest request) {
        cartService.updateQuantity(buyerId, itemId, request.getQuantity());
        return ResponseEntity.ok("Quantity updated");
    }

    @GetMapping("/result")
    public ResponseEntity<List<CartResultResponse>> getCartResult(@AuthenticationPrincipal(expression = "id") Long buyerId) {
        return ResponseEntity.ok(cartService.getCartResult(buyerId));
    }
}

package com.shop.backend.controller;

import com.shop.backend.dto.PlaceOrderRequest;
import com.shop.backend.dto.UpdateOrderStatusRequest;
import com.shop.backend.dto.OrderResponse;
import com.shop.backend.service.AuthUser;
import com.shop.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/placeorder")
    public ResponseEntity<?> placeOrder(
            @AuthenticationPrincipal AuthUser currentUser,
            @RequestBody PlaceOrderRequest request) {
        Long buyerId = Long.parseLong(currentUser.getUserId());
        orderService.placeOrder(buyerId, request);
        return ResponseEntity.ok("Order placed successfully");
    }

    @GetMapping("/buyer-order-history")
    public ResponseEntity<List<OrderResponse>> getBuyerOrderHistory(
            @AuthenticationPrincipal AuthUser currentUser) {
        Long buyerId = Long.parseLong(currentUser.getUserId());
        List<OrderResponse> orders = orderService.getOrderHistory(buyerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/seller-orders")
    public ResponseEntity<List<OrderResponse>> getSellerOrders(
            @AuthenticationPrincipal AuthUser currentUser) {
        Long sellerId = Long.parseLong(currentUser.getUserId());
        List<OrderResponse> orders = orderService.getSellerOrders(sellerId);
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/seller-orders/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @AuthenticationPrincipal AuthUser currentUser,
            @PathVariable Long orderId,
            @RequestBody UpdateOrderStatusRequest request) {
        Long sellerId = Long.parseLong(currentUser.getUserId());
        OrderResponse updatedOrder = orderService.updateOrderStatus(orderId, request.getStatus(), sellerId);
        return ResponseEntity.ok(updatedOrder);
    }
}

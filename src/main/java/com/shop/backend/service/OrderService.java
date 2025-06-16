package com.shop.backend.service;

import com.shop.backend.dto.PlaceOrderRequest;
import com.shop.backend.dto.UpdateOrderStatusRequest;
import com.shop.backend.dto.OrderItemResponse;
import com.shop.backend.dto.OrderResponse;
import com.shop.backend.entity.*;
import com.shop.backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public void placeOrder(Long buyerId, PlaceOrderRequest request) {

        List<Long> productIds = request.getItems().stream()
                .map(PlaceOrderRequest.Item::getProduct_id)
                .collect(Collectors.toList());
        System.out.println("PlaceOrder API productIds"+productIds);

        List<Product> products = productRepository.findByIdIn(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        System.out.println("PlaceOrder API products "+products);

        for (PlaceOrderRequest.Item item : request.getItems()) {
            Product product = productMap.get(item.getProduct_id());
            if (product == null || product.getQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product ID " + item.getProduct_id());
            }
        }

        Map<Long, List<PlaceOrderRequest.Item>> groupedItems = new HashMap<>();
        for (PlaceOrderRequest.Item item : request.getItems()) {
            Product product = productMap.get(item.getProduct_id());
            groupedItems.computeIfAbsent(product.getSeller().getId(), k -> new ArrayList<>()).add(item);
        }

        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new IllegalArgumentException("Buyer not found"));

        for (Map.Entry<Long, List<PlaceOrderRequest.Item>> entry : groupedItems.entrySet()) {
            List<PlaceOrderRequest.Item> sellerItems = entry.getValue();

            BigDecimal totalAmount = BigDecimal.ZERO;

            List<OrderItem> orderItems = new ArrayList<>();

            for (PlaceOrderRequest.Item item : sellerItems) {
                Product product = productMap.get(item.getProduct_id());
                BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                totalAmount = totalAmount.add(totalPrice);

                OrderItem orderItem = OrderItem.builder()
                        .product(product)
                        .quantity(item.getQuantity())
                        .unitPrice(product.getPrice())
                        .totalPrice(totalPrice)
                        .build();
                orderItems.add(orderItem);

                product.setQuantity(product.getQuantity() - item.getQuantity());
            }

            Order order = Order.builder()
                    .buyer(buyer)
                    .orderDate(LocalDateTime.now())
                    .totalAmount(totalAmount)
                    .status("Pending")
                    .address(request.getAddress())
                    .items(orderItems)
                    .build();

            orderItems.forEach(oi -> oi.setOrder(order));

            orderRepository.save(order);
            productRepository.saveAll(products);
        }
    }

    public List<OrderResponse> getOrderHistory(Long buyerId) {
        User buyer = userRepository.findById(buyerId).orElseThrow(() -> new IllegalArgumentException("Buyer not found"));

        List<Order> orders = orderRepository.findByBuyer(buyer);

        return orders.stream().map(order -> OrderResponse.builder()
                .orderId(order.getId())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .address(order.getAddress())
                .totalAmount(order.getTotalAmount())
                .items(order.getItems().stream().map(item -> OrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .build()).toList())
                .build()).toList();
    }

    public List<OrderResponse> getSellerOrders(Long sellerId) {

        List<Order> orders = orderRepository.findAll();
        List<OrderResponse> sellerOrders = new ArrayList<>();

        for (Order order : orders) {
            List<OrderItem> sellerItems = order.getItems().stream()
                    .filter(oi -> oi.getProduct().getSeller().getId().equals(sellerId))
                    .toList();

            if (!sellerItems.isEmpty()) {
                BigDecimal totalAmount = sellerItems.stream()
                        .map(OrderItem::getTotalPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                List<OrderItemResponse> itemResponses = sellerItems.stream()
                        .map(oi -> OrderItemResponse.builder()
                                .productId(oi.getProduct().getId())
                                .productName(oi.getProduct().getName())
                                .quantity(oi.getQuantity())
                                .unitPrice(oi.getUnitPrice())
                                .totalPrice(oi.getTotalPrice())
                                .build())
                        .toList();

                OrderResponse response = OrderResponse.builder()
                        .orderId(order.getId())
                        .orderDate(order.getOrderDate())
                        .status(order.getStatus())
                        .address(order.getAddress())
                        .totalAmount(totalAmount)
                        .items(itemResponses)
                        .build();

                sellerOrders.add(response);
            }
        }

        return sellerOrders;
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String newStatus, Long sellerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        boolean belongsToSeller = order.getItems().stream()
                .anyMatch(oi -> oi.getProduct().getSeller().getId().equals(sellerId));

        if (!belongsToSeller) {
            throw new AccessDeniedException("You are not authorized to update this order");
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        return OrderResponse.builder()
                .orderId(updatedOrder.getId())
                .orderDate(updatedOrder.getOrderDate())
                .status(updatedOrder.getStatus())
                .address(updatedOrder.getAddress())
                .totalAmount(updatedOrder.getTotalAmount())
                .items(updatedOrder.getItems().stream().map(item -> OrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .build()).toList())
                .build();
    }
}


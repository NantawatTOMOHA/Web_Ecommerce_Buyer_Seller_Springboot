package com.shop.backend.dto;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long orderId;
    private LocalDateTime orderDate;
    private String status;
    private String address;
    private BigDecimal totalAmount;
    private String buyerName;
    private List<OrderItemResponse> items;
}

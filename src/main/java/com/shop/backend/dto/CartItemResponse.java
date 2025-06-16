package com.shop.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {
    private Long id;
    private String productName;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
}

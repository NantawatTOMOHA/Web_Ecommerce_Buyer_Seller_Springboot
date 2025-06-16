package com.shop.backend.dto;

import lombok.Data;

@Data
public class CartResultResponse {
    private Long productId;
    private int quantity;
}

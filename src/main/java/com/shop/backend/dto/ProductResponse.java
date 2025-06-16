package com.shop.backend.dto;
import lombok.Data;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private Long sellerId;
    private java.time.LocalDateTime createdAt;
    private String imageBase64;
}

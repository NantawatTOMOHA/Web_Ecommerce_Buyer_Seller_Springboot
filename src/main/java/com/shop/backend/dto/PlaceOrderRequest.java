package com.shop.backend.dto;
import lombok.Data;

import java.util.List;

@Data
public class PlaceOrderRequest {
    private List<Item> items;
    private String address;

    @Data
    public static class Item {
        private Long product_id;
        private int quantity;
    }
}

package com.shop.backend.repository;

import com.shop.backend.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem,Long> {
    List<CartItem> findByCartId(Long CartId);
    void deleteByCartId(Long cartId);
}

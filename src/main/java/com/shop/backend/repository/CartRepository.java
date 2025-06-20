package com.shop.backend.repository;

import com.shop.backend.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface CartRepository extends JpaRepository<Cart, Long>{
    Optional<Cart> findFirstByBuyerIdOrderByCreatedAtDesc(Long buyerId);
    Optional<Cart> findByBuyerId(Long buyerId);
}

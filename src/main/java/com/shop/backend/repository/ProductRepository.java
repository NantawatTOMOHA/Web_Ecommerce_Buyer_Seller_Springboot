package com.shop.backend.repository;

import com.shop.backend.entity.Product;
import com.shop.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByIdIn(List<Long> ids);
    List<Product> findByIsDeletedFalseOrderByCreatedAtDesc();
    List<Product> findBySellerAndIsDeletedFalseOrderByCreatedAtDesc(User seller);
    List<Product> findByNameContainingIgnoreCaseAndIsDeletedFalse(String keyword);
    List<Product> findBySellerAndNameContainingIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDesc(User seller, String keyword);
}

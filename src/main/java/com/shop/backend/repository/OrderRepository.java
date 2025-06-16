package com.shop.backend.repository;
import com.shop.backend.entity.Order;
import com.shop.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface OrderRepository extends JpaRepository<Order, Long>{
    List<Order> findByBuyer(User buyer);
}

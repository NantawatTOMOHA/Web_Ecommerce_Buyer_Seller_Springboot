package com.shop.backend.service;


import com.shop.backend.dto.CartItemRequest;
import com.shop.backend.dto.CartItemResponse;
import com.shop.backend.dto.CartResultResponse;   
import com.shop.backend.entity.Cart;
import com.shop.backend.entity.CartItem;
import com.shop.backend.entity.Product;
import com.shop.backend.entity.User;
import com.shop.backend.repository.CartItemRepository;
import com.shop.backend.repository.CartRepository;
import com.shop.backend.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepo;
    private final CartItemRepository cartItemRepo;
    private final ProductRepository productRepo;

    public List<CartItemResponse> getCart(Long buyerId) {
        return cartRepo.findByBuyerId(buyerId)
                .map(cart -> cart.getItems().stream().map(item -> {
                    CartItemResponse dto = new CartItemResponse();
                    dto.setId(item.getId());
                    dto.setProductName(item.getProduct().getName());
                    dto.setQuantity(item.getQuantity());
                    dto.setUnitPrice(item.getUnitPrice().doubleValue());
                    dto.setTotalPrice(item.getTotalPrice().doubleValue());
                    return dto;
                }).collect(Collectors.toList()))
                .orElse(List.of());
    }

    @Transactional
    public void addToCart(Long buyerId, CartItemRequest request) {
        Product product = productRepo.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Cart cart = cartRepo.findByBuyerId(buyerId).orElseGet(() -> {
            Cart newCart = Cart.builder()
                    .buyer(User.builder().id(buyerId).build())
                    .createdAt(LocalDateTime.now())
                    .build();
            return cartRepo.save(newCart);
        });

        BigDecimal total = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        CartItem item = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(request.getQuantity())
                .unitPrice(product.getPrice())
                .totalPrice(total)
                .build();

        cartItemRepo.save(item);
    }

    @Transactional
    public void deleteCartItem(Long buyerId, Long itemId) {
        Optional<CartItem> item = cartItemRepo.findById(itemId);
        if (item.isPresent() && item.get().getCart().getBuyer().getId().equals(buyerId)) {
            cartItemRepo.deleteById(itemId);
        } else {
            throw new RuntimeException("No permission to delete");
        }
    }

    @Transactional
    public void clearCart(Long buyerId) {
        cartRepo.findByBuyerId(buyerId).ifPresent(cart -> {
            cartItemRepo.deleteByCartId(cart.getId());
        });
    }

    @Transactional
    public void updateQuantity(Long buyerId, Long itemId, int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("Quantity must be > 0");

        CartItem item = cartItemRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("CartItem not found"));

        if (!item.getCart().getBuyer().getId().equals(buyerId)) {
            throw new RuntimeException("Not authorized");
        }

        item.setQuantity(quantity);
        item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
        cartItemRepo.save(item);
    }

    public List<CartResultResponse> getCartResult(Long buyerId) {
        return cartRepo.findFirstByBuyerIdOrderByCreatedAtDesc(buyerId)
                .map(cart -> cartItemRepo.findByCartId(cart.getId()).stream().map(item -> {
                    CartResultResponse dto = new CartResultResponse();
                    dto.setProductId(item.getProduct().getId());
                    dto.setQuantity(item.getQuantity());
                    return dto;
                }).collect(Collectors.toList()))
                .orElse(List.of());
    }
}

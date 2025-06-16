package com.shop.backend.service;

import com.shop.backend.dto.ProductRequest;
import com.shop.backend.entity.Product;
import com.shop.backend.entity.User;
import com.shop.backend.repository.ProductRepository;
import com.shop.backend.repository.UserRepository;
import com.shop.backend.util.CryptoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CryptoUtil cryptoUtil;

    @Transactional
    public Product addProduct(ProductRequest request, long userId) throws Exception {
        User seller = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));
        Product.ProductBuilder builder = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .seller(seller)
                .createdAt(LocalDateTime.now())
                .isDeleted(false);

        if (request.getImageBase64() != null && cryptoUtil.isValidBase64Image(request.getImageBase64())) {
            byte[] imageBytes = cryptoUtil.parseBase64Image(request.getImageBase64());
            builder.image(cryptoUtil.encrypt(imageBytes));
        }

        return productRepository.save(builder.build());
    }

    public List<Product> getAllProducts() {
        return productRepository.findByIsDeletedFalseOrderByCreatedAtDesc();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .filter(p -> !p.getIsDeleted())
                .orElse(null);
    }

    public Product getProductByIdPublic(Long id) {
        return productRepository.findById(id)
                .filter(p -> !p.getIsDeleted())
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCaseAndIsDeletedFalse(keyword);
    }

    public List<Product> getMyProducts(long userId) {
        User seller = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));
        return productRepository.findBySellerAndIsDeletedFalseOrderByCreatedAtDesc(seller);
    }

    public List<Product> searchMyProducts(long userId, String keyword) {
        User seller = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));
        return productRepository.findBySellerAndNameContainingIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDesc(seller, keyword);
    }

    @Transactional
    public Product updateProduct(Long id, ProductRequest request, long userId) throws Exception {
        Product product = productRepository.findById(id)
                .filter(p -> !p.getIsDeleted() && p.getSeller().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Not authorized to edit this product"));

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getQuantity() != null) product.setQuantity(request.getQuantity());

        if (request.getImageBase64() != null) {
            if (!cryptoUtil.isValidBase64Image(request.getImageBase64())) {
                throw new RuntimeException("Invalid image format");
            }
            byte[] imageBytes = cryptoUtil.parseBase64Image(request.getImageBase64());
            product.setImage(cryptoUtil.encrypt(imageBytes));
        }

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id, Long userId) {
        Product product = productRepository.findById(id)
                .filter(p -> !p.getIsDeleted() && p.getSeller().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Not authorized to delete this product"));

        product.setIsDeleted(true);
        productRepository.save(product);
    }
}

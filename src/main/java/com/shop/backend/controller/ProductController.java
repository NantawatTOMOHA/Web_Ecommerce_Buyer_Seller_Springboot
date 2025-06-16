package com.shop.backend.controller;
import com.shop.backend.dto.ProductRequest;
import com.shop.backend.dto.ProductResponse;
import com.shop.backend.entity.Product;
import com.shop.backend.service.AuthUser;
import com.shop.backend.service.ProductService;
import com.shop.backend.util.CryptoUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CryptoUtil cryptoUtil;

    // GET /api/products/allproduct
    @GetMapping("/allproduct")
    public ResponseEntity<List<ProductResponse>> getAllProducts(@RequestParam(required = false) String keyword) {
        List<Product> products = (keyword == null || keyword.isEmpty()) ?
                productService.getAllProducts() :
                productService.searchProducts(keyword);

        List<ProductResponse> response = products.stream()
                .map(this::toProductResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // GET /api/products/my-products
    @GetMapping("/my-products")
    public ResponseEntity<List<ProductResponse>> getMyProducts(
            @AuthenticationPrincipal AuthUser currentUser,
            @RequestParam(required = false) String keyword) {
        Long userId = Long.parseLong(currentUser.getUserId());
        List<Product> products = (keyword == null || keyword.isEmpty()) ?
                productService.getMyProducts(userId) :
                productService.searchMyProducts(userId, keyword);
        System.out.println("My P API called");
        List<ProductResponse> response = products.stream()
                .map(this::toProductResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // POST /api/products/add
    @PostMapping("/add")
    public ResponseEntity<ProductResponse> addProduct(
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal AuthUser currentUser) throws Exception {
        System.out.println("ADD P API called");
        Long userId = Long.parseLong(currentUser.getUserId());
        Product product = productService.addProduct(request, userId);
        return ResponseEntity.ok(toProductResponse(product));
    }

    // GET /api/products/search/{keyword}
    @GetMapping("/search/{keyword}")
    public ResponseEntity<List<ProductResponse>> searchProducts(@PathVariable String keyword) {
        List<Product> products = productService.searchProducts(keyword);
        List<ProductResponse> response = products.stream()
                .map(this::toProductResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    // GET /api/products/seller/search/{keyword}
    @GetMapping("/seller/search/{keyword}")
    public ResponseEntity<List<ProductResponse>> searchMyProducts(
            @PathVariable String keyword,
            @AuthenticationPrincipal AuthUser currentUser) {

        Long userId = Long.parseLong(currentUser.getUserId());
        List<Product> products = productService.searchMyProducts(userId, keyword);
        List<ProductResponse> response = products.stream()
                .map(this::toProductResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    // GET /api/products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toProductResponse(product));
    }


    // PUT /api/products/edit/{id}
    @PutMapping("/edit/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal AuthUser currentUser) throws Exception {
        Long userId = Long.parseLong(currentUser.getUserId());
        System.out.println("Edit P API called");
        Product product = productService.updateProduct(id, request, userId);
        return ResponseEntity.ok(toProductResponse(product));
    }

    // DELETE /api/products/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthUser currentUser) {
        Long userId = Long.parseLong(currentUser.getUserId());
        System.out.println("del P API called");
        productService.deleteProduct(id, userId);
        return ResponseEntity.ok().build();
    }

    private ProductResponse toProductResponse(Product product) {
        String imageBase64 = null;
        try {
            if (product.getImage() != null) {
                byte[] decryptedImage = cryptoUtil.decrypt(product.getImage());
                imageBase64 = cryptoUtil.toBase64Image(decryptedImage);
            }
        } catch (Exception e) {
            // Log error or set imageBase64 = null
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .sellerId(product.getSeller().getId())
                .createdAt(product.getCreatedAt())
                .imageBase64(imageBase64)
                .build();
    }

}

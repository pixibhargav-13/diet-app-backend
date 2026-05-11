package com.dietapp.shopmanagement.controller;

import com.dietapp.api.shop.api.OrdersApi;
import com.dietapp.api.shop.api.ProductsApi;
import com.dietapp.api.shop.api.ReviewsApi;
import com.dietapp.api.shop.api.WishlistApi;
import com.dietapp.api.shop.model.*;
import com.dietapp.sessionmanagement.exception.ResourceNotFoundException;
import com.dietapp.shopmanagement.service.ShopService;
import com.dietapp.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ShopController implements ProductsApi, ReviewsApi, OrdersApi, WishlistApi {

    private final ShopService shopService;
    private final UserRepository userRepository;

    // ── Products ──────────────────────────────────────────────────────────────

    @Override
    public ResponseEntity<List<ProductResponse>> getProducts(String category, String search) {
        return ResponseEntity.ok(shopService.getProducts(category, search));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(ProductRequest productRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shopService.createProduct(productRequest));
    }

    @Override
    public ResponseEntity<ProductResponse> getProduct(Long id) {
        return ResponseEntity.ok(shopService.getProduct(id));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(Long id, ProductRequest productRequest) {
        return ResponseEntity.ok(shopService.updateProduct(id, productRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(Long id) {
        shopService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateStock(Long id, UpdateStockRequest updateStockRequest) {
        return ResponseEntity.ok(shopService.updateStock(id, updateStockRequest));
    }

    // ── Reviews ───────────────────────────────────────────────────────────────

    @Override
    public ResponseEntity<List<ProductReview>> getProductReviews(Long id) {
        return ResponseEntity.ok(shopService.getProductReviews(id));
    }

    @Override
    public ResponseEntity<ProductReview> submitReview(Long id, ReviewRequest reviewRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shopService.submitReview(currentUserId(), id, reviewRequest));
    }

    // ── Orders ────────────────────────────────────────────────────────────────

    @Override
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        return ResponseEntity.ok(shopService.getMyOrders(currentUserId()));
    }

    @Override
    public ResponseEntity<OrderResponse> placeOrder(PlaceOrderRequest placeOrderRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shopService.placeOrder(currentUserId(), placeOrderRequest));
    }

    @Override
    public ResponseEntity<OrderResponse> getOrder(Long id) {
        return ResponseEntity.ok(shopService.getOrder(id));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders(String status) {
        return ResponseEntity.ok(shopService.getAllOrders(status));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(Long id, UpdateOrderStatusRequest updateOrderStatusRequest) {
        return ResponseEntity.ok(shopService.updateOrderStatus(id, updateOrderStatusRequest));
    }

    // ── Wishlist ──────────────────────────────────────────────────────────────

    @Override
    public ResponseEntity<WishlistResponse> getWishlist() {
        return ResponseEntity.ok(shopService.getWishlist(currentUserId()));
    }

    @Override
    public ResponseEntity<WishlistResponse> addToWishlist(Long productId) {
        return ResponseEntity.ok(shopService.addToWishlist(currentUserId(), productId));
    }

    @Override
    public ResponseEntity<WishlistResponse> removeFromWishlist(Long productId) {
        return ResponseEntity.ok(shopService.removeFromWishlist(currentUserId(), productId));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Long currentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email))
                .getId();
    }
}

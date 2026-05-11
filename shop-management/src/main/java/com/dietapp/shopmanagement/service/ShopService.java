package com.dietapp.shopmanagement.service;

import com.dietapp.api.shop.model.*;
import com.dietapp.sessionmanagement.exception.ResourceNotFoundException;
import com.dietapp.shopmanagement.model.entity.*;
import com.dietapp.shopmanagement.repository.*;
import com.dietapp.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ProductRepository productRepository;
    private final ProductReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;

    // ── Products ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ProductResponse> getProducts(String category, String search) {
        List<ProductEntity> entities;
        if (category != null && search != null) {
            entities = productRepository.findByCategoryAndNameContainingIgnoreCaseAndActiveTrue(category, search);
        } else if (category != null) {
            entities = productRepository.findByCategoryAndActiveTrue(category);
        } else if (search != null) {
            entities = productRepository.findByNameContainingIgnoreCaseAndActiveTrue(search);
        } else {
            entities = productRepository.findByActiveTrue();
        }
        return entities.stream().map(this::toProductResponse).toList();
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        ProductEntity entity = buildProductEntity(new ProductEntity(), request);
        return toProductResponse(productRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        return toProductResponse(findProductOrThrow(id));
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        return toProductResponse(productRepository.save(buildProductEntity(findProductOrThrow(id), request)));
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.delete(findProductOrThrow(id));
    }

    @Transactional
    public ProductResponse updateStock(Long id, UpdateStockRequest request) {
        ProductEntity entity = findProductOrThrow(id);
        entity.setStockLevel(request.getStockLevel());
        return toProductResponse(productRepository.save(entity));
    }

    // ── Reviews ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ProductReview> getProductReviews(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream().map(this::toReview).toList();
    }

    @Transactional
    public ProductReview submitReview(Long userId, Long productId, ReviewRequest request) {
        ProductReviewEntity entity = reviewRepository.findByProductIdAndUserId(productId, userId)
                .orElseGet(() -> ProductReviewEntity.builder()
                        .productId(productId).userId(userId).rating(0).build());
        entity.setRating(request.getRating());
        entity.setComment(request.getComment());
        return toReview(reviewRepository.save(entity));
    }

    // ── Orders ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toOrderResponse).toList();
    }

    @Transactional
    public OrderResponse placeOrder(Long userId, PlaceOrderRequest request) {
        BigDecimal subtotal = BigDecimal.ZERO;
        OrderEntity order = OrderEntity.builder()
                .userId(userId)
                .status("pending")
                .paymentMethod(request.getPaymentMethod().getValue())
                .deliveryLabel(request.getDeliveryAddress().getLabel())
                .deliveryLine1(request.getDeliveryAddress().getLine1())
                .deliveryLine2(request.getDeliveryAddress().getLine2())
                .deliveryPhone(request.getDeliveryAddress().getPhone())
                .subtotal(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .build();

        for (PlaceOrderRequestItemsInner item : request.getItems()) {
            ProductEntity product = findProductOrThrow(item.getProductId());
            BigDecimal unitPrice = product.getPrice();
            BigDecimal itemSubtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);

            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .order(order)
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(item.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(itemSubtotal)
                    .build();
            order.getItems().add(orderItem);
        }

        order.setSubtotal(subtotal);
        order.setTotal(subtotal.add(order.getHandlingFee()));
        return toOrderResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        return toOrderResponse(findOrderOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders(String status) {
        List<OrderEntity> entities = status != null
                ? orderRepository.findByStatusOrderByCreatedAtDesc(status)
                : orderRepository.findAllByOrderByCreatedAtDesc();
        return entities.stream().map(this::toOrderResponse).toList();
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request) {
        OrderEntity entity = findOrderOrThrow(id);
        entity.setStatus(request.getStatus().getValue());
        return toOrderResponse(orderRepository.save(entity));
    }

    // ── Wishlist ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public WishlistResponse getWishlist(Long userId) {
        List<Long> productIds = wishlistRepository.findByUserId(userId)
                .stream().map(WishlistEntity::getProductId).toList();
        return new WishlistResponse().userId(userId).productIds(productIds);
    }

    @Transactional
    public WishlistResponse addToWishlist(Long userId, Long productId) {
        findProductOrThrow(productId);
        if (!wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            wishlistRepository.save(WishlistEntity.builder().userId(userId).productId(productId).build());
        }
        return getWishlist(userId);
    }

    @Transactional
    public WishlistResponse removeFromWishlist(Long userId, Long productId) {
        wishlistRepository.findByUserIdAndProductId(userId, productId)
                .ifPresent(wishlistRepository::delete);
        return getWishlist(userId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ProductEntity findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    private OrderEntity findOrderOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    private ProductEntity buildProductEntity(ProductEntity entity, ProductRequest req) {
        entity.setName(req.getName());
        entity.setDescription(req.getDescription());
        entity.setPrice(req.getPrice());
        entity.setCategory(req.getCategory());
        entity.setImageUrl(req.getImageUrl());
        entity.setNutritionInfo(req.getNutritionInfo());
        entity.setIngredients(req.getIngredients());
        if (req.getStockLevel() != null) entity.setStockLevel(req.getStockLevel());
        if (req.getActive() != null) entity.setActive(req.getActive());
        return entity;
    }

    private ProductResponse toProductResponse(ProductEntity e) {
        Double avgRatingRaw = reviewRepository.findAverageRatingByProductId(e.getId());
        BigDecimal avgRating = avgRatingRaw != null ? BigDecimal.valueOf(avgRatingRaw) : null;
        long reviewCount = reviewRepository.findByProductIdOrderByCreatedAtDesc(e.getId()).size();

        return new ProductResponse()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .price(e.getPrice())
                .category(e.getCategory())
                .imageUrl(e.getImageUrl())
                .stockLevel(e.getStockLevel())
                .active(e.getActive())
                .nutritionInfo(e.getNutritionInfo())
                .ingredients(e.getIngredients())
                .averageRating(avgRating)
                .reviewCount((int) reviewCount)
                .createdAt(e.getCreatedAt() != null ? e.getCreatedAt().atOffset(ZoneOffset.UTC) : null);
    }

    private ProductReview toReview(ProductReviewEntity e) {
        String userName = userRepository.findById(e.getUserId())
                .map(u -> u.getFirstName() + " " + u.getLastName()).orElse("Unknown");
        return new ProductReview()
                .id(e.getId())
                .productId(e.getProductId())
                .userId(e.getUserId())
                .userName(userName)
                .rating(e.getRating())
                .comment(e.getComment())
                .createdAt(e.getCreatedAt() != null ? e.getCreatedAt().atOffset(ZoneOffset.UTC) : null);
    }

    private OrderResponse toOrderResponse(OrderEntity e) {
        String clientName = userRepository.findById(e.getUserId())
                .map(u -> u.getFirstName() + " " + u.getLastName()).orElse("Unknown");

        List<OrderItem> items = e.getItems().stream().map(i ->
                new OrderItem()
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .subtotal(i.getSubtotal())
        ).toList();

        DeliveryAddress address = new DeliveryAddress()
                .label(e.getDeliveryLabel())
                .line1(e.getDeliveryLine1())
                .line2(e.getDeliveryLine2())
                .phone(e.getDeliveryPhone());

        OrderResponse r = new OrderResponse()
                .id(e.getId())
                .userId(e.getUserId())
                .clientName(clientName)
                .items(items)
                .subtotal(e.getSubtotal())
                .handlingFee(e.getHandlingFee())
                .total(e.getTotal())
                .paymentMethod(e.getPaymentMethod())
                .deliveryAddress(address)
                .createdAt(e.getCreatedAt() != null ? e.getCreatedAt().atOffset(ZoneOffset.UTC) : null);

        if (e.getStatus() != null) {
            try { r.setStatus(OrderResponse.StatusEnum.fromValue(e.getStatus())); } catch (Exception ignored) {}
        }
        return r;
    }
}

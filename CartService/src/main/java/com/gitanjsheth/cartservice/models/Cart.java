package com.gitanjsheth.cartservice.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Document(collection = "carts")
public class Cart {
    
    @Id
    private String id;
    
    @Indexed
    private Long userId; // null for guest carts
    
    @Indexed
    private String sessionId; // for guest users
    
    private List<CartItem> items = new ArrayList<>();
    
    private CartStatus status = CartStatus.ACTIVE;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Indexed
    private LocalDateTime expiresAt;
    
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    private Integer totalItems = 0;
    
    private Integer totalQuantity = 0;
    
    // Constructor for user cart
    public Cart(Long userId) {
        this.userId = userId;
        this.expiresAt = LocalDateTime.now().plusDays(30); // User carts expire after 30 days
    }
    
    // Constructor for guest cart
    public Cart(String sessionId) {
        this.sessionId = sessionId;
        this.expiresAt = LocalDateTime.now().plusDays(7); // Guest carts expire after 7 days
    }
    
    // Default constructor for MongoDB
    public Cart() {
        this.expiresAt = LocalDateTime.now().plusDays(30);
    }
    
    // Business methods
    
    public void addItem(CartItem item) {
        // Check if item already exists
        CartItem existingItem = findItemByProductId(item.getProductId());
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
            existingItem.updateSubtotal();
            existingItem.setUpdatedAt(LocalDateTime.now());
        } else {
            items.add(item);
        }
        updateTotals();
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean removeItem(Long productId) {
        boolean removed = items.removeIf(item -> item.getProductId().equals(productId));
        if (removed) {
            updateTotals();
            this.updatedAt = LocalDateTime.now();
        }
        return removed;
    }
    
    public boolean updateItemQuantity(Long productId, Integer newQuantity) {
        CartItem item = findItemByProductId(productId);
        if (item != null) {
            if (newQuantity <= 0) {
                return removeItem(productId);
            } else {
                item.setQuantity(newQuantity);
                item.updateSubtotal();
                item.setUpdatedAt(LocalDateTime.now());
                updateTotals();
                this.updatedAt = LocalDateTime.now();
                return true;
            }
        }
        return false;
    }
    
    public CartItem findItemByProductId(Long productId) {
        return items.stream()
            .filter(item -> item.getProductId().equals(productId))
            .findFirst()
            .orElse(null);
    }
    
    public void clearItems() {
        items.clear();
        updateTotals();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void updateTotals() {
        this.totalItems = items.size();
        this.totalQuantity = items.stream()
            .mapToInt(CartItem::getQuantity)
            .sum();
        this.totalAmount = items.stream()
            .map(CartItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public boolean isEmpty() {
        return items.isEmpty();
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public void extendExpiry(int days) {
        this.expiresAt = LocalDateTime.now().plusDays(days);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markAsCheckedOut() {
        this.status = CartStatus.CHECKED_OUT;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markAsAbandoned() {
        this.status = CartStatus.ABANDONED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markAsExpired() {
        this.status = CartStatus.EXPIRED;
        this.updatedAt = LocalDateTime.now();
    }
    
    // Convert guest cart to user cart
    public void convertToUserCart(Long userId) {
        this.userId = userId;
        this.sessionId = null;
        this.expiresAt = LocalDateTime.now().plusDays(30);
        this.updatedAt = LocalDateTime.now();
    }
}
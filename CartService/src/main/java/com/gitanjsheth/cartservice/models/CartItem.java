package com.gitanjsheth.cartservice.models;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CartItem {
    
    private Long productId;
    
    private String productTitle;
    
    private String productImageUrl;
    
    private BigDecimal unitPrice;
    
    private Integer quantity;
    
    private BigDecimal subtotal;
    
    private LocalDateTime addedAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    // Product availability info (cached from product service)
    private Boolean isAvailable = true;
    
    private Boolean isOutOfStock = false;
    
    private Integer availableQuantity;
    
    // Default constructor
    public CartItem() {}
    
    // Constructor
    public CartItem(Long productId, String productTitle, String productImageUrl, 
                   BigDecimal unitPrice, Integer quantity) {
        this.productId = productId;
        this.productTitle = productTitle;
        this.productImageUrl = productImageUrl;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        updateSubtotal();
    }
    
    // Business methods
    
    public void updateSubtotal() {
        if (unitPrice != null && quantity != null) {
            this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    public void increaseQuantity(int amount) {
        this.quantity += amount;
        updateSubtotal();
    }
    
    public void decreaseQuantity(int amount) {
        this.quantity = Math.max(0, this.quantity - amount);
        updateSubtotal();
    }
    
    public boolean isQuantityAvailable() {
        return availableQuantity == null || quantity <= availableQuantity;
    }
    
    public void updateAvailability(Boolean isAvailable, Boolean isOutOfStock, Integer availableQuantity) {
        this.isAvailable = isAvailable;
        this.isOutOfStock = isOutOfStock;
        this.availableQuantity = availableQuantity;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean hasAvailabilityIssues() {
        return !isAvailable || isOutOfStock || !isQuantityAvailable();
    }
}
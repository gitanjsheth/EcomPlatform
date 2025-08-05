package com.gitanjsheth.cartservice.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CartItemDto {
    
    private Long productId;
    
    private String productTitle;
    
    private String productImageUrl;
    
    private BigDecimal unitPrice;
    
    private Integer quantity;
    
    private BigDecimal subtotal;
    
    private LocalDateTime addedAt;
    
    private LocalDateTime updatedAt;
    
    private Boolean isAvailable;
    
    private Boolean isOutOfStock;
    
    private Integer availableQuantity;
    
    private String availabilityMessage; // User-friendly availability message
}
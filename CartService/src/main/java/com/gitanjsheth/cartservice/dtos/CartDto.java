package com.gitanjsheth.cartservice.dtos;

import com.gitanjsheth.cartservice.models.CartStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class CartDto {
    
    private String id;
    
    private Long userId;
    
    private String sessionId;
    
    private List<CartItemDto> items;
    
    private CartStatus status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime expiresAt;
    
    private BigDecimal totalAmount;
    
    private Integer totalItems;
    
    private Integer totalQuantity;
    
    private Boolean hasAvailabilityIssues = false;
    
    private String message; // For informational messages
}
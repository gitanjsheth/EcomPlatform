package com.gitanjsheth.cartservice.utils;

import com.gitanjsheth.cartservice.dtos.CartDto;
import com.gitanjsheth.cartservice.dtos.CartItemDto;
import com.gitanjsheth.cartservice.models.Cart;
import com.gitanjsheth.cartservice.models.CartItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {
    
    public CartDto toDto(Cart cart) {
        if (cart == null) {
            return null;
        }
        
        CartDto dto = new CartDto();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUserId());
        dto.setSessionId(cart.getSessionId());
        dto.setStatus(cart.getStatus());
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());
        dto.setExpiresAt(cart.getExpiresAt());
        dto.setTotalAmount(cart.getTotalAmount());
        dto.setTotalItems(cart.getTotalItems());
        dto.setTotalQuantity(cart.getTotalQuantity());
        
        // Map cart items
        List<CartItemDto> itemDtos = cart.getItems().stream()
            .map(this::toItemDto)
            .collect(Collectors.toList());
        dto.setItems(itemDtos);
        
        // Check for availability issues
        boolean hasIssues = cart.getItems().stream()
            .anyMatch(CartItem::hasAvailabilityIssues);
        dto.setHasAvailabilityIssues(hasIssues);
        
        if (hasIssues) {
            dto.setMessage("Some items in your cart may have availability issues. Please review before checkout.");
        }
        
        return dto;
    }
    
    public CartItemDto toItemDto(CartItem item) {
        if (item == null) {
            return null;
        }
        
        CartItemDto dto = new CartItemDto();
        dto.setProductId(item.getProductId());
        dto.setProductTitle(item.getProductTitle());
        dto.setProductImageUrl(item.getProductImageUrl());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setQuantity(item.getQuantity());
        dto.setSubtotal(item.getSubtotal());
        dto.setAddedAt(item.getAddedAt());
        dto.setUpdatedAt(item.getUpdatedAt());
        dto.setIsAvailable(item.getIsAvailable());
        dto.setIsOutOfStock(item.getIsOutOfStock());
        dto.setAvailableQuantity(item.getAvailableQuantity());
        
        // Generate availability message
        dto.setAvailabilityMessage(generateAvailabilityMessage(item));
        
        return dto;
    }
    
    private String generateAvailabilityMessage(CartItem item) {
        if (!item.getIsAvailable()) {
            return "This product is no longer available";
        }
        
        if (item.getIsOutOfStock()) {
            return "This product is currently out of stock";
        }
        
        if (item.getAvailableQuantity() != null && item.getQuantity() > item.getAvailableQuantity()) {
            return String.format("Only %d items available (you have %d in cart)", 
                item.getAvailableQuantity(), item.getQuantity());
        }
        
        if (item.getAvailableQuantity() != null && item.getAvailableQuantity() < 5) {
            return String.format("Only %d items left in stock", item.getAvailableQuantity());
        }
        
        return null; // No issues
    }
}
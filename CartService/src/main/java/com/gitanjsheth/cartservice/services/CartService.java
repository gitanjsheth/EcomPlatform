package com.gitanjsheth.cartservice.services;

import com.gitanjsheth.cartservice.dtos.AddToCartDto;
import com.gitanjsheth.cartservice.dtos.CartDto;
import com.gitanjsheth.cartservice.dtos.UpdateCartItemDto;

public interface CartService {
    
    // Get cart operations
    CartDto getCart(Long userId, String sessionId);
    CartDto getCartById(String cartId);
    
    // Cart modification operations
    CartDto addToCart(Long userId, String sessionId, AddToCartDto addToCartDto);
    CartDto updateCartItem(Long userId, String sessionId, Long productId, UpdateCartItemDto updateDto);
    CartDto removeFromCart(Long userId, String sessionId, Long productId);
    CartDto clearCart(Long userId, String sessionId);
    
    // Cart management operations
    CartDto mergeGuestCartWithUserCart(String sessionId, Long userId);
    void expireCart(String cartId);
    void cleanupExpiredCarts();
    void validateCartInventory(String cartId);
    void markCartAsCheckedOut(String cartId);
    
    // Utility operations
    int getCartItemCount(Long userId, String sessionId);
    boolean isProductInCart(Long userId, String sessionId, Long productId);
}
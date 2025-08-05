package com.gitanjsheth.cartservice.repositories;

import com.gitanjsheth.cartservice.models.Cart;
import com.gitanjsheth.cartservice.models.CartStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends MongoRepository<Cart, String> {
    
    // Find cart by user ID
    Optional<Cart> findByUserIdAndStatus(Long userId, CartStatus status);
    
    // Find cart by session ID (for guest users)
    Optional<Cart> findBySessionIdAndStatus(String sessionId, CartStatus status);
    
    // Find all active carts for a user
    List<Cart> findByUserIdAndStatusIn(Long userId, List<CartStatus> statuses);
    
    // Find expired carts for cleanup
    List<Cart> findByExpiresAtBefore(LocalDateTime dateTime);
    
    // Find abandoned carts (not updated for X hours)
    @Query("{ 'updatedAt': { $lt: ?0 }, 'status': 'ACTIVE' }")
    List<Cart> findAbandonedCarts(LocalDateTime cutoffTime);
    
    // Find carts containing specific product
    @Query("{ 'items.productId': ?0, 'status': 'ACTIVE' }")
    List<Cart> findActiveCartsContainingProduct(Long productId);
    
    // Count active carts by user
    long countByUserIdAndStatus(Long userId, CartStatus status);
    
    // Delete carts by status
    void deleteByStatus(CartStatus status);
    
    // Find carts that need inventory validation
    @Query("{ 'updatedAt': { $lt: ?0 }, 'status': 'ACTIVE' }")
    List<Cart> findCartsNeedingInventoryCheck(LocalDateTime lastCheckTime);
}
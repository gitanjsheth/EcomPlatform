package com.gitanjsheth.cartservice.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitanjsheth.cartservice.models.Cart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@Slf4j
public class CartCacheRepository {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${app.cart.cache-ttl-hours:1}")
    private int cacheTtlHours;
    
    @Value("${app.cart.guest-cart-ttl-hours:24}")
    private int guestCartTtlHours;
    
    public CartCacheRepository(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }
    
    public void cacheCart(Cart cart) {
        try {
            String key = generateCacheKey(cart);
            String cartJson = objectMapper.writeValueAsString(cart);
            
            Duration ttl = cart.getUserId() != null 
                ? Duration.ofHours(cacheTtlHours) 
                : Duration.ofHours(guestCartTtlHours);
            
            redisTemplate.opsForValue().set(key, cartJson, ttl);
            log.debug("Cached cart: {}", key);
            
        } catch (JsonProcessingException e) {
            log.error("Error caching cart: {}", e.getMessage());
        }
    }
    
    public Optional<Cart> getCachedCart(Long userId, String sessionId) {
        try {
            String key = generateCacheKey(userId, sessionId);
            String cartJson = redisTemplate.opsForValue().get(key);
            
            if (cartJson != null) {
                Cart cart = objectMapper.readValue(cartJson, Cart.class);
                log.debug("Retrieved cached cart: {}", key);
                return Optional.of(cart);
            }
            
        } catch (JsonProcessingException e) {
            log.error("Error retrieving cached cart: {}", e.getMessage());
        }
        
        return Optional.empty();
    }
    
    public void invalidateCart(Long userId, String sessionId) {
        String key = generateCacheKey(userId, sessionId);
        redisTemplate.delete(key);
        log.debug("Invalidated cart cache: {}", key);
    }
    
    public void invalidateCart(Cart cart) {
        String key = generateCacheKey(cart);
        redisTemplate.delete(key);
        log.debug("Invalidated cart cache: {}", key);
    }
    
    // Cache product availability temporarily
    public void cacheProductAvailability(Long productId, boolean available, int availableQuantity) {
        try {
            String key = "product:availability:" + productId;
            String data = objectMapper.writeValueAsString(new ProductAvailability(available, availableQuantity));
            redisTemplate.opsForValue().set(key, data, Duration.ofMinutes(10)); // 10-minute cache
            
        } catch (JsonProcessingException e) {
            log.error("Error caching product availability: {}", e.getMessage());
        }
    }
    
    public Optional<ProductAvailability> getCachedProductAvailability(Long productId) {
        try {
            String key = "product:availability:" + productId;
            String data = redisTemplate.opsForValue().get(key);
            
            if (data != null) {
                return Optional.of(objectMapper.readValue(data, ProductAvailability.class));
            }
            
        } catch (JsonProcessingException e) {
            log.error("Error retrieving cached product availability: {}", e.getMessage());
        }
        
        return Optional.empty();
    }
    
    private String generateCacheKey(Cart cart) {
        return generateCacheKey(cart.getUserId(), cart.getSessionId());
    }
    
    private String generateCacheKey(Long userId, String sessionId) {
        if (userId != null) {
            return "cart:user:" + userId;
        } else if (sessionId != null) {
            return "cart:session:" + sessionId;
        } else {
            throw new IllegalArgumentException("Either userId or sessionId must be provided");
        }
    }
    
    // Inner class for product availability caching
    public static class ProductAvailability {
        private boolean available;
        private int availableQuantity;
        
        public ProductAvailability() {}
        
        public ProductAvailability(boolean available, int availableQuantity) {
            this.available = available;
            this.availableQuantity = availableQuantity;
        }
        
        // Getters and setters
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
        public int getAvailableQuantity() { return availableQuantity; }
        public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }
    }
}
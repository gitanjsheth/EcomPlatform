package com.gitanjsheth.cartservice.services;

import com.gitanjsheth.cartservice.dtos.AddToCartDto;
import com.gitanjsheth.cartservice.dtos.CartDto;
import com.gitanjsheth.cartservice.dtos.UpdateCartItemDto;
import com.gitanjsheth.cartservice.exceptions.CartNotFoundException;
import com.gitanjsheth.cartservice.exceptions.InvalidCartOperationException;
import com.gitanjsheth.cartservice.exceptions.ProductNotAvailableException;
import com.gitanjsheth.cartservice.models.Cart;
import com.gitanjsheth.cartservice.models.CartItem;
import com.gitanjsheth.cartservice.models.CartStatus;
import com.gitanjsheth.cartservice.repositories.CartCacheRepository;
import com.gitanjsheth.cartservice.repositories.CartRepository;
import com.gitanjsheth.cartservice.utils.CartMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CartServiceImpl implements CartService {
    
    private final CartRepository cartRepository;
    private final CartCacheRepository cartCacheRepository;
    private final ProductValidationService productValidationService;
    private final CartMapper cartMapper;
    
    @Value("${app.cart.max-items-per-cart:100}")
    private int maxItemsPerCart;
    
    @Value("${app.cart.inventory-check-enabled:true}")
    private boolean inventoryCheckEnabled;
    
    public CartServiceImpl(CartRepository cartRepository,
                          CartCacheRepository cartCacheRepository,
                          ProductValidationService productValidationService,
                          CartMapper cartMapper) {
        this.cartRepository = cartRepository;
        this.cartCacheRepository = cartCacheRepository;
        this.productValidationService = productValidationService;
        this.cartMapper = cartMapper;
    }
    
    @Override
    public CartDto getCart(Long userId, String sessionId) {
        // Try cache first
        Optional<Cart> cachedCart = cartCacheRepository.getCachedCart(userId, sessionId);
        if (cachedCart.isPresent() && !cachedCart.get().isExpired()) {
            Cart cart = cachedCart.get();
            if (inventoryCheckEnabled) {
                validateAndUpdateCartInventory(cart);
            }
            return cartMapper.toDto(cart);
        }
        
        // Fetch from database
        Optional<Cart> cart = findActiveCart(userId, sessionId);
        if (cart.isPresent()) {
            Cart foundCart = cart.get();
            if (foundCart.isExpired()) {
                foundCart.markAsExpired();
                cartRepository.save(foundCart);
                return cartMapper.toDto(createNewCart(userId, sessionId));
            }
            
            if (inventoryCheckEnabled) {
                validateAndUpdateCartInventory(foundCart);
            }
            
            // Cache the cart
            cartCacheRepository.cacheCart(foundCart);
            return cartMapper.toDto(foundCart);
        }
        
        // Create new cart if none exists
        Cart newCart = createNewCart(userId, sessionId);
        return cartMapper.toDto(newCart);
    }
    
    @Override
    public CartDto getCartById(String cartId) {
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new CartNotFoundException("Cart not found with id: " + cartId));
        
        if (inventoryCheckEnabled) {
            validateAndUpdateCartInventory(cart);
        }
        
        return cartMapper.toDto(cart);
    }
    
    @Override
    @Transactional
    public CartDto addToCart(Long userId, String sessionId, AddToCartDto addToCartDto) {
        // Validate product availability
        if (inventoryCheckEnabled && !productValidationService.isProductAvailableForCart(
                addToCartDto.getProductId(), addToCartDto.getQuantity())) {
            throw new ProductNotAvailableException("Product is not available for the requested quantity");
        }
        
        // Get or create cart
        Cart cart = findOrCreateCart(userId, sessionId);
        
        // Check cart limits
        if (cart.getItems().size() >= maxItemsPerCart) {
            throw new InvalidCartOperationException("Cart has reached maximum items limit");
        }
        
        // Get product details
        var productDetails = productValidationService.getProductDetails(addToCartDto.getProductId());
        
        // Create cart item
        CartItem cartItem = new CartItem(
            addToCartDto.getProductId(),
            productDetails.getTitle(),
            productDetails.getImageUrl(),
            productDetails.getPrice(),
            addToCartDto.getQuantity()
        );
        
        // Add item to cart
        cart.addItem(cartItem);
        
        // Save cart
        cart = cartRepository.save(cart);
        
        // Update cache
        cartCacheRepository.cacheCart(cart);
        
        log.info("Added product {} (quantity: {}) to cart for user/session: {}/{}", 
            addToCartDto.getProductId(), addToCartDto.getQuantity(), userId, sessionId);
        
        return cartMapper.toDto(cart);
    }
    
    @Override
    @Transactional
    public CartDto updateCartItem(Long userId, String sessionId, Long productId, UpdateCartItemDto updateDto) {
        Cart cart = findActiveCart(userId, sessionId)
            .orElseThrow(() -> new CartNotFoundException("Cart not found"));
        
        if (updateDto.getQuantity() == 0) {
            // Remove item
            cart.removeItem(productId);
        } else {
            // Validate inventory if enabled
            if (inventoryCheckEnabled && !productValidationService.isProductAvailableForCart(
                    productId, updateDto.getQuantity())) {
                throw new ProductNotAvailableException("Product is not available for the requested quantity");
            }
            
            // Update quantity
            if (!cart.updateItemQuantity(productId, updateDto.getQuantity())) {
                throw new InvalidCartOperationException("Product not found in cart");
            }
        }
        
        // Save cart
        cart = cartRepository.save(cart);
        
        // Update cache
        cartCacheRepository.cacheCart(cart);
        
        log.info("Updated product {} quantity to {} in cart for user/session: {}/{}", 
            productId, updateDto.getQuantity(), userId, sessionId);
        
        return cartMapper.toDto(cart);
    }
    
    @Override
    @Transactional
    public CartDto removeFromCart(Long userId, String sessionId, Long productId) {
        Cart cart = findActiveCart(userId, sessionId)
            .orElseThrow(() -> new CartNotFoundException("Cart not found"));
        
        if (!cart.removeItem(productId)) {
            throw new InvalidCartOperationException("Product not found in cart");
        }
        
        // Save cart
        cart = cartRepository.save(cart);
        
        // Update cache
        cartCacheRepository.cacheCart(cart);
        
        log.info("Removed product {} from cart for user/session: {}/{}", productId, userId, sessionId);
        
        return cartMapper.toDto(cart);
    }
    
    @Override
    @Transactional
    public CartDto clearCart(Long userId, String sessionId) {
        Cart cart = findActiveCart(userId, sessionId)
            .orElseThrow(() -> new CartNotFoundException("Cart not found"));
        
        cart.clearItems();
        
        // Save cart
        cart = cartRepository.save(cart);
        
        // Update cache
        cartCacheRepository.cacheCart(cart);
        
        log.info("Cleared cart for user/session: {}/{}", userId, sessionId);
        
        return cartMapper.toDto(cart);
    }
    
    @Override
    @Transactional
    public CartDto mergeGuestCartWithUserCart(String sessionId, Long userId) {
        // Find guest cart
        Optional<Cart> guestCart = cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE);
        if (guestCart.isEmpty()) {
            // No guest cart to merge, return user's existing cart
            return getCart(userId, null);
        }
        
        // Find existing user cart
        Optional<Cart> userCart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE);
        
        Cart targetCart;
        if (userCart.isPresent() && !userCart.get().isEmpty()) {
            // User has existing non-empty cart - keep user cart, suggest adding guest items
            targetCart = userCart.get();
            
            // Add guest cart items that don't already exist
            for (CartItem guestItem : guestCart.get().getItems()) {
                CartItem existingItem = targetCart.findItemByProductId(guestItem.getProductId());
                if (existingItem == null) {
                    // Product not in user cart, add it
                    if (inventoryCheckEnabled && productValidationService.isProductAvailableForCart(
                            guestItem.getProductId(), guestItem.getQuantity())) {
                        targetCart.addItem(guestItem);
                    }
                }
                // If product exists, keep user's quantity (don't overwrite)
            }
            
            log.info("Merged guest cart items into existing user cart for user: {}", userId);
        } else {
            // User has empty cart or no cart, convert guest cart to user cart
            targetCart = guestCart.get();
            targetCart.convertToUserCart(userId);
            
            log.info("Converted guest cart to user cart for user: {}", userId);
        }
        
        // Mark guest cart as merged
        guestCart.get().setStatus(CartStatus.MERGED);
        cartRepository.save(guestCart.get());
        
        // Save target cart
        targetCart = cartRepository.save(targetCart);
        
        // Update cache
        cartCacheRepository.invalidateCart(null, sessionId);
        cartCacheRepository.cacheCart(targetCart);
        
        return cartMapper.toDto(targetCart);
    }
    
    @Override
    public void expireCart(String cartId) {
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new CartNotFoundException("Cart not found"));
        
        cart.setStatus(CartStatus.EXPIRED);
        cartRepository.save(cart);
        
        // Invalidate cache
        cartCacheRepository.invalidateCart(cart);
        
        log.info("Expired cart: {}", cartId);
    }
    
    @Override
    @Scheduled(fixedRateString = "${app.cart.cleanup-interval-hours:6}000000") // 6 hours in milliseconds
    public void cleanupExpiredCarts() {
        LocalDateTime cutoffTime = LocalDateTime.now();
        List<Cart> expiredCarts = cartRepository.findByExpiresAtBefore(cutoffTime);
        
        for (Cart cart : expiredCarts) {
            cart.setStatus(CartStatus.EXPIRED);
            cartRepository.save(cart);
            cartCacheRepository.invalidateCart(cart);
        }
        
        log.info("Cleaned up {} expired carts", expiredCarts.size());
        
        // Also clean up abandoned carts (not updated in 48 hours)
        LocalDateTime abandonedCutoff = LocalDateTime.now().minusHours(48);
        List<Cart> abandonedCarts = cartRepository.findAbandonedCarts(abandonedCutoff);
        
        for (Cart cart : abandonedCarts) {
            cart.markAsAbandoned();
            cartRepository.save(cart);
            cartCacheRepository.invalidateCart(cart);
        }
        
        log.info("Marked {} carts as abandoned", abandonedCarts.size());
    }
    
    @Override
    public void validateCartInventory(String cartId) {
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new CartNotFoundException("Cart not found"));
        
        validateAndUpdateCartInventory(cart);
        cartRepository.save(cart);
        cartCacheRepository.cacheCart(cart);
    }
    

    
    @Override
    public int getCartItemCount(Long userId, String sessionId) {
        Optional<Cart> cart = findActiveCart(userId, sessionId);
        return cart.map(Cart::getTotalItems).orElse(0);
    }
    
    @Override
    public boolean isProductInCart(Long userId, String sessionId, Long productId) {
        Optional<Cart> cart = findActiveCart(userId, sessionId);
        return cart.map(c -> c.findItemByProductId(productId) != null).orElse(false);
    }
    
    // Private helper methods
    
    private Optional<Cart> findActiveCart(Long userId, String sessionId) {
        if (userId != null) {
            return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE);
        } else if (sessionId != null) {
            return cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE);
        }
        return Optional.empty();
    }
    
    private Cart findOrCreateCart(Long userId, String sessionId) {
        Optional<Cart> existingCart = findActiveCart(userId, sessionId);
        return existingCart.orElseGet(() -> createNewCart(userId, sessionId));
    }
    
    private Cart createNewCart(Long userId, String sessionId) {
        Cart cart;
        if (userId != null) {
            cart = new Cart(userId);
        } else {
            cart = new Cart(sessionId);
        }
        return cartRepository.save(cart);
    }
    
    private void validateAndUpdateCartInventory(Cart cart) {
        boolean hasChanges = false;
        
        for (CartItem item : cart.getItems()) {
            var availability = productValidationService.checkProductAvailability(item.getProductId());
            
            boolean wasAvailable = item.getIsAvailable();
            item.updateAvailability(
                availability.isAvailable(), 
                availability.isOutOfStock(), 
                availability.getAvailableQuantity()
            );
            
            if (wasAvailable != item.getIsAvailable()) {
                hasChanges = true;
                log.info("Updated availability for product {} in cart {}: available={}, outOfStock={}", 
                    item.getProductId(), cart.getId(), item.getIsAvailable(), item.getIsOutOfStock());
            }
        }
        
        if (hasChanges) {
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);
            cartCacheRepository.cacheCart(cart);
        }
    }
}
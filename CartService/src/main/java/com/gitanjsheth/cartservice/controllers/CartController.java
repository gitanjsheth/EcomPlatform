package com.gitanjsheth.cartservice.controllers;

import com.gitanjsheth.cartservice.dtos.AddToCartDto;
import com.gitanjsheth.cartservice.dtos.CartDto;
import com.gitanjsheth.cartservice.dtos.UpdateCartItemDto;
import com.gitanjsheth.cartservice.services.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:8081"})
@Slf4j
public class CartController {
    
    private final CartService cartService;
    
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }
    
    @GetMapping
    public ResponseEntity<CartDto> getCart(HttpServletRequest request) {
        Long userId = extractUserId(request);
        String sessionId = extractSessionId(request);
        
        CartDto cart = cartService.getCart(userId, sessionId);
        return ResponseEntity.ok(cart);
    }
    
    @GetMapping("/{cartId}")
    public ResponseEntity<CartDto> getCartById(@PathVariable String cartId) {
        CartDto cart = cartService.getCartById(cartId);
        return ResponseEntity.ok(cart);
    }
    
    @PostMapping("/add")
    public ResponseEntity<CartDto> addToCart(@Valid @RequestBody AddToCartDto addToCartDto,
                                           HttpServletRequest request) {
        Long userId = extractUserId(request);
        String sessionId = extractSessionId(request);
        
        // Use sessionId from request if not provided in DTO
        if (addToCartDto.getSessionId() == null) {
            addToCartDto.setSessionId(sessionId);
        }
        
        CartDto cart = cartService.addToCart(userId, sessionId, addToCartDto);
        return ResponseEntity.ok(cart);
    }
    
    @PutMapping("/items/{productId}")
    public ResponseEntity<CartDto> updateCartItem(@PathVariable Long productId,
                                                @Valid @RequestBody UpdateCartItemDto updateDto,
                                                HttpServletRequest request) {
        Long userId = extractUserId(request);
        String sessionId = extractSessionId(request);
        
        CartDto cart = cartService.updateCartItem(userId, sessionId, productId, updateDto);
        return ResponseEntity.ok(cart);
    }
    
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartDto> removeFromCart(@PathVariable Long productId,
                                                HttpServletRequest request) {
        Long userId = extractUserId(request);
        String sessionId = extractSessionId(request);
        
        CartDto cart = cartService.removeFromCart(userId, sessionId, productId);
        return ResponseEntity.ok(cart);
    }
    
    @DeleteMapping("/clear")
    public ResponseEntity<CartDto> clearCart(HttpServletRequest request) {
        Long userId = extractUserId(request);
        String sessionId = extractSessionId(request);
        
        CartDto cart = cartService.clearCart(userId, sessionId);
        return ResponseEntity.ok(cart);
    }
    
    @PostMapping("/merge")
    public ResponseEntity<CartDto> mergeGuestCart(@RequestParam String sessionId,
                                                HttpServletRequest request) {
        Long userId = extractUserId(request);
        
        if (userId == null) {
            return ResponseEntity.badRequest().build(); // User must be authenticated
        }
        
        CartDto cart = cartService.mergeGuestCartWithUserCart(sessionId, userId);
        return ResponseEntity.ok(cart);
    }
    
    @GetMapping("/count")
    public ResponseEntity<Integer> getCartItemCount(HttpServletRequest request) {
        Long userId = extractUserId(request);
        String sessionId = extractSessionId(request);
        
        int count = cartService.getCartItemCount(userId, sessionId);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/contains/{productId}")
    public ResponseEntity<Boolean> isProductInCart(@PathVariable Long productId,
                                                 HttpServletRequest request) {
        Long userId = extractUserId(request);
        String sessionId = extractSessionId(request);
        
        boolean contains = cartService.isProductInCart(userId, sessionId, productId);
        return ResponseEntity.ok(contains);
    }
    
    @PostMapping("/{cartId}/validate")
    public ResponseEntity<Void> validateCartInventory(@PathVariable String cartId) {
        cartService.validateCartInventory(cartId);
        return ResponseEntity.ok().build();
    }
    
    // Helper methods
    
    private Long extractUserId(HttpServletRequest request) {
        // This would be populated by JWT authentication filter
        Object userIdAttr = request.getAttribute("userId");
        return userIdAttr != null ? Long.valueOf(userIdAttr.toString()) : null;
    }
    
    private String extractSessionId(HttpServletRequest request) {
        // Try to get from header first, then fall back to HTTP session
        String sessionId = request.getHeader("X-Session-ID");
        if (sessionId == null) {
            sessionId = request.getSession().getId();
        }
        return sessionId;
    }
}
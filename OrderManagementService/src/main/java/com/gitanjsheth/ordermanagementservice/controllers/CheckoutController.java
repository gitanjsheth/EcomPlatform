package com.gitanjsheth.ordermanagementservice.controllers;

import com.gitanjsheth.ordermanagementservice.dtos.CreateOrderDto;
import com.gitanjsheth.ordermanagementservice.dtos.OrderDto;
import com.gitanjsheth.ordermanagementservice.services.OrderService;
import com.gitanjsheth.ordermanagementservice.services.CartIntegrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Public user-facing checkout endpoints
 * Provides the bridge between Cart and Order services
 */
@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:8081"})
public class CheckoutController {
    
    private final OrderService orderService;
    private final CartIntegrationService cartIntegrationService;
    
    /**
     * Initiate checkout process - validates cart and returns checkout summary
     * This is the entry point for users to start the checkout flow
     */
    @PostMapping("/initiate")
    public ResponseEntity<Map<String, Object>> initiateCheckout(
            @RequestParam String cartId,
            HttpServletRequest request) {
        
        Long userId = extractUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Authentication required",
                "message", "User must be logged in to checkout"
            ));
        }
        
        try {
            // Validate cart and get checkout summary
            Map<String, Object> checkoutSummary = cartIntegrationService.prepareCheckoutSummary(cartId, userId);
            return ResponseEntity.ok(checkoutSummary);
            
        } catch (Exception e) {
            log.error("Error initiating checkout for cart {} and user {}", cartId, userId, e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Checkout initiation failed",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Complete checkout and create order
     * This is the main endpoint users call to complete their purchase
     */
    @PostMapping("/complete")
    public ResponseEntity<OrderDto> completeCheckout(
            @Valid @RequestBody CreateOrderDto createOrderDto,
            HttpServletRequest request) {
        
        Long userId = extractUserId(request);
        if (userId == null) {
            throw new RuntimeException("Authentication required");
        }
        
        try {
            // Validate cart ownership and availability
            cartIntegrationService.validateCartForCheckout(createOrderDto.getCartId(), userId);
            
            // Create order from cart
            OrderDto order = orderService.createOrderFromCart(createOrderDto, userId);
            
            // Mark cart as checked out
            cartIntegrationService.markCartAsCheckedOut(createOrderDto.getCartId());
            
            log.info("Checkout completed successfully for user {} with order {}", userId, order.getId());
            return ResponseEntity.ok(order);
            
        } catch (Exception e) {
            log.error("Error completing checkout for user {} with cart {}", userId, createOrderDto.getCartId(), e);
            throw e;
        }
    }
    
    /**
     * Get checkout summary for a cart
     * Allows users to review their order before completing checkout
     */
    @GetMapping("/summary/{cartId}")
    public ResponseEntity<Map<String, Object>> getCheckoutSummary(
            @PathVariable String cartId,
            HttpServletRequest request) {
        
        Long userId = extractUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Authentication required",
                "message", "User must be logged in to view checkout summary"
            ));
        }
        
        try {
            Map<String, Object> summary = cartIntegrationService.prepareCheckoutSummary(cartId, userId);
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("Error getting checkout summary for cart {} and user {}", cartId, userId, e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get checkout summary",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Validate cart before checkout
     * Checks inventory, pricing, and cart validity
     */
    @PostMapping("/validate/{cartId}")
    public ResponseEntity<Map<String, Object>> validateCartForCheckout(
            @PathVariable String cartId,
            HttpServletRequest request) {
        
        Long userId = extractUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Authentication required",
                "message", "User must be logged in to validate cart"
            ));
        }
        
        try {
            Map<String, Object> validationResult = cartIntegrationService.validateCartForCheckout(cartId, userId);
            return ResponseEntity.ok(validationResult);
            
        } catch (Exception e) {
            log.error("Error validating cart {} for user {}", cartId, userId, e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Cart validation failed",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Get available payment methods for checkout
     */
    @GetMapping("/payment-methods")
    public ResponseEntity<Map<String, Object>> getAvailablePaymentMethods(HttpServletRequest request) {
        Long userId = extractUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Authentication required",
                "message", "User must be logged in to view payment methods"
            ));
        }
        
        // Return available payment methods
        Map<String, Object> paymentMethods = Map.of(
            "availableMethods", new String[]{"CREDIT_CARD", "DEBIT_CARD", "PAYPAL", "BANK_TRANSFER"},
            "defaultMethod", "CREDIT_CARD",
            "supportedCurrencies", new String[]{"USD", "EUR", "GBP"}
        );
        
        return ResponseEntity.ok(paymentMethods);
    }
    
    /**
     * Get shipping options for checkout
     */
    @GetMapping("/shipping-options")
    public ResponseEntity<Map<String, Object>> getShippingOptions(
            @RequestParam String cartId,
            HttpServletRequest request) {
        
        Long userId = extractUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Authentication required",
                "message", "User must be logged in to view shipping options"
            ));
        }
        
        try {
            Map<String, Object> shippingOptions = cartIntegrationService.getShippingOptions(cartId, userId);
            return ResponseEntity.ok(shippingOptions);
            
        } catch (Exception e) {
            log.error("Error getting shipping options for cart {} and user {}", cartId, userId, e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get shipping options",
                "message", e.getMessage()
            ));
        }
    }
    
    // Helper methods
    
    private Long extractUserId(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        
        // Fallback to request attribute
        Object userIdAttr = request.getAttribute("userId");
        return userIdAttr != null ? Long.valueOf(userIdAttr.toString()) : null;
    }
}

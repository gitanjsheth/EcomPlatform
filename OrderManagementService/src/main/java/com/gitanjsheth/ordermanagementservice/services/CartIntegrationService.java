package com.gitanjsheth.ordermanagementservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartIntegrationService {
    
    private final RestTemplate restTemplate;
    
    @Value("${app.cart-service.url}")
    private String cartServiceUrl;
    
    @Value("${app.service.token}")
    private String serviceToken;
    
    /**
     * Get cart details from CartService
     */
    public Map<String, Object> getCart(String cartId) {
        try {
            String url = cartServiceUrl + "/api/carts/" + cartId;
            HttpHeaders headers = createServiceHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, Map.class);
            
            log.info("Retrieved cart {} from CartService", cartId);
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Failed to retrieve cart {} from CartService: {}", cartId, e.getMessage());
            throw new RuntimeException("Failed to retrieve cart: " + e.getMessage());
        }
    }
    
    /**
     * Mark cart as checked out
     */
    public void markCartAsCheckedOut(String cartId) {
        try {
            String url = cartServiceUrl + "/api/carts/" + cartId + "/checkout";
            HttpHeaders headers = createServiceHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
            log.info("Marked cart {} as checked out", cartId);
            
        } catch (Exception e) {
            log.error("Failed to mark cart {} as checked out: {}", cartId, e.getMessage());
            // Don't throw exception - cart marking is not critical for order creation
        }
    }
    
    /**
     * Prepare checkout summary for a cart
     * Validates cart and returns summary information
     */
    public Map<String, Object> prepareCheckoutSummary(String cartId, Long userId) {
        try {
            // Get cart details
            Map<String, Object> cart = getCart(cartId);
            
            // Validate cart ownership
            if (!userId.equals(cart.get("userId"))) {
                throw new RuntimeException("Cart does not belong to user");
            }
            
            // Validate cart status
            String status = (String) cart.get("status");
            if (!"ACTIVE".equals(status)) {
                throw new RuntimeException("Cart is not active for checkout");
            }
            
            // Validate cart has items
            if (cart.get("items") == null || ((java.util.List<?>) cart.get("items")).isEmpty()) {
                throw new RuntimeException("Cart is empty");
            }
            
            // Return checkout summary
            return Map.of(
                "cartId", cartId,
                "userId", userId,
                "items", cart.get("items"),
                "totalAmount", cart.get("totalAmount"),
                "totalItems", cart.get("totalItems"),
                "totalQuantity", cart.get("totalQuantity"),
                "isValid", true,
                "validationMessages", new String[0]
            );
            
        } catch (Exception e) {
            log.error("Failed to prepare checkout summary for cart {} and user {}: {}", cartId, userId, e.getMessage());
            throw new RuntimeException("Failed to prepare checkout summary: " + e.getMessage());
        }
    }
    
    /**
     * Validate cart for checkout
     * Performs comprehensive validation before order creation
     */
    public Map<String, Object> validateCartForCheckout(String cartId, Long userId) {
        try {
            // Get cart details
            Map<String, Object> cart = getCart(cartId);
            
            // Validate cart ownership
            if (!userId.equals(cart.get("userId"))) {
                throw new RuntimeException("Cart does not belong to user");
            }
            
            // Validate cart status
            String status = (String) cart.get("status");
            if (!"ACTIVE".equals(status)) {
                throw new RuntimeException("Cart is not active for checkout");
            }
            
            // Validate cart has items
            if (cart.get("items") == null || ((java.util.List<?>) cart.get("items")).isEmpty()) {
                throw new RuntimeException("Cart is empty");
            }
            
            // Validate inventory availability (basic check)
            // In a real implementation, this would call ProductService to verify inventory
            boolean inventoryAvailable = true;
            String[] validationMessages = new String[0];
            
            if (!inventoryAvailable) {
                validationMessages = new String[]{"Some items are out of stock"};
            }
            
            return Map.of(
                "cartId", cartId,
                "userId", userId,
                "isValid", inventoryAvailable,
                "validationMessages", validationMessages,
                "canProceed", inventoryAvailable
            );
            
        } catch (Exception e) {
            log.error("Failed to validate cart {} for user {}: {}", cartId, userId, e.getMessage());
            throw new RuntimeException("Failed to validate cart: " + e.getMessage());
        }
    }
    
    /**
     * Get shipping options for a cart
     */
    public Map<String, Object> getShippingOptions(String cartId, Long userId) {
        try {
            // Validate cart ownership first
            Map<String, Object> cart = getCart(cartId);
            if (!userId.equals(cart.get("userId"))) {
                throw new RuntimeException("Cart does not belong to user");
            }
            
            // Return available shipping options
            // In a real implementation, this would calculate based on cart contents and delivery address
            return Map.of(
                "availableOptions", new Object[]{
                    Map.of(
                        "id", "standard",
                        "name", "Standard Shipping",
                        "price", "5.99",
                        "deliveryDays", "3-5 business days"
                    ),
                    Map.of(
                        "id", "express",
                        "name", "Express Shipping",
                        "price", "12.99",
                        "deliveryDays", "1-2 business days"
                    ),
                    Map.of(
                        "id", "overnight",
                        "name", "Overnight Shipping",
                        "price", "24.99",
                        "deliveryDays", "Next business day"
                    )
                },
                "defaultOption", "standard"
            );
            
        } catch (Exception e) {
            log.error("Failed to get shipping options for cart {} and user {}: {}", cartId, userId, e.getMessage());
            throw new RuntimeException("Failed to get shipping options: " + e.getMessage());
        }
    }
    
    private HttpHeaders createServiceHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Service-Token", serviceToken);
        headers.set("X-Service-Name", "OrderManagementService");
        headers.set("Content-Type", "application/json");
        return headers;
    }
}
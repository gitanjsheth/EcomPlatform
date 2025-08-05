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

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductIntegrationService {
    
    private final RestTemplate restTemplate;
    
    @Value("${app.product-service.url}")
    private String productServiceUrl;
    
    @Value("${app.service.token}")
    private String serviceToken;
    
    /**
     * Reserve inventory for order items (24-hour hold)
     */
    public boolean reserveInventory(Long productId, Integer quantity) {
        try {
            String url = productServiceUrl + "/products/" + productId + "/inventory/reserve";
            HttpHeaders headers = createServiceHeaders();
            
            Map<String, Object> request = Map.of("quantity", quantity);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, Map.class);
            
            boolean success = (Boolean) response.getBody().get("success");
            log.info("Inventory reservation for product {} quantity {}: {}", 
                productId, quantity, success ? "SUCCESS" : "FAILED");
            
            return success;
            
        } catch (Exception e) {
            log.error("Failed to reserve inventory for product {} quantity {}: {}", 
                productId, quantity, e.getMessage());
            return false;
        }
    }
    
    /**
     * Release inventory reservation
     */
    public void releaseInventory(Long productId, Integer quantity) {
        try {
            String url = productServiceUrl + "/products/" + productId + "/inventory/release";
            HttpHeaders headers = createServiceHeaders();
            
            Map<String, Object> request = Map.of("quantity", quantity);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
            log.info("Released inventory for product {} quantity {}", productId, quantity);
            
        } catch (Exception e) {
            log.error("Failed to release inventory for product {} quantity {}: {}", 
                productId, quantity, e.getMessage());
        }
    }
    
    /**
     * Confirm inventory usage (final stock deduction)
     */
    public void confirmInventoryUsage(Long productId, Integer quantity) {
        try {
            String url = productServiceUrl + "/products/" + productId + "/inventory/confirm";
            HttpHeaders headers = createServiceHeaders();
            
            Map<String, Object> request = Map.of("quantity", quantity);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
            log.info("Confirmed inventory usage for product {} quantity {}", productId, quantity);
            
        } catch (Exception e) {
            log.error("Failed to confirm inventory usage for product {} quantity {}: {}", 
                productId, quantity, e.getMessage());
        }
    }
    
    /**
     * Validate cart items availability
     */
    public Map<String, Object> validateCartItems(List<Map<String, Object>> cartItems) {
        try {
            String url = productServiceUrl + "/products/validate-cart";
            HttpHeaders headers = createServiceHeaders();
            
            Map<String, Object> request = Map.of("items", cartItems);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, Map.class);
            
            log.info("Validated cart items: {}", response.getBody());
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Failed to validate cart items: {}", e.getMessage());
            throw new RuntimeException("Failed to validate cart items: " + e.getMessage());
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
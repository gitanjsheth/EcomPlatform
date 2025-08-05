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
    
    private HttpHeaders createServiceHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Service-Token", serviceToken);
        headers.set("X-Service-Name", "OrderManagementService");
        headers.set("Content-Type", "application/json");
        return headers;
    }
}
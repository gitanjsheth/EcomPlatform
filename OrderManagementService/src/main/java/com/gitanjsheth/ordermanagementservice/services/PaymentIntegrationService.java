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

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentIntegrationService {
    
    private final RestTemplate restTemplate;
    
    @Value("${app.payment-service.url}")
    private String paymentServiceUrl;
    
    @Value("${app.service.token}")
    private String serviceToken;
    
    /**
     * Process payment for order
     */
    public Map<String, Object> processPayment(String orderId, BigDecimal amount, String currency) {
        try {
            String url = paymentServiceUrl + "/api/payments/process";
            HttpHeaders headers = createServiceHeaders();
            
            Map<String, Object> request = Map.of(
                "orderId", orderId,
                "amount", amount.doubleValue(),
                "currency", currency
            );
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, Map.class);
            
            log.info("Payment processing response for order {}: {}", orderId, response.getBody());
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Failed to process payment for order {}: {}", orderId, e.getMessage());
            throw new RuntimeException("Payment processing failed: " + e.getMessage());
        }
    }
    
    /**
     * Initiate refund for order
     */
    public Map<String, Object> initiateRefund(String paymentId, BigDecimal amount, String reason) {
        try {
            String url = paymentServiceUrl + "/api/payments/" + paymentId + "/refund";
            HttpHeaders headers = createServiceHeaders();
            
            Map<String, Object> request = Map.of(
                "amount", amount.doubleValue(),
                "reason", reason
            );
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, Map.class);
            
            log.info("Refund initiated for payment {}: {}", paymentId, response.getBody());
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Failed to initiate refund for payment {}: {}", paymentId, e.getMessage());
            throw new RuntimeException("Refund initiation failed: " + e.getMessage());
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
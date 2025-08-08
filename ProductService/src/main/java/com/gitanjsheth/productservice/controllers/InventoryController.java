package com.gitanjsheth.productservice.controllers;

import com.gitanjsheth.productservice.dtos.ProductAvailabilityDto;
import com.gitanjsheth.productservice.services.InventoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:8081", "http://localhost:8082"})
@Slf4j
public class InventoryController {
    
    private final InventoryService inventoryService;
    @Value("${app.service.token:}")
    private String configuredServiceToken;
    
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    
    @GetMapping("/{productId}/availability")
    public ResponseEntity<ProductAvailabilityDto> getProductAvailability(@PathVariable Long productId) {
        try {
            ProductAvailabilityDto availability = inventoryService.getProductAvailability(productId);
            return ResponseEntity.ok(availability);
        } catch (Exception e) {
            log.error("Error getting product availability for product {}: {}", productId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{productId}/inventory/reserve")
    public ResponseEntity<Void> reserveInventory(@PathVariable Long productId, 
                                                @RequestBody ReservationRequest request,
                                                HttpServletRequest httpRequest) {
        if (!isValidServiceRequest(httpRequest)) {
            log.warn("Unauthorized inventory reserve request from {}", httpRequest.getRemoteAddr());
            return ResponseEntity.status(403).build();
        }
        
        try {
            boolean success = inventoryService.reserveInventoryForCheckout(
                productId, request.getQuantity(), request.getUserId());
            
            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            log.error("Error reserving inventory for product {}: {}", productId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/{productId}/inventory/release")
    public ResponseEntity<Void> releaseInventory(@PathVariable Long productId,
                                               @RequestBody ReservationRequest request,
                                               HttpServletRequest httpRequest) {
        if (!isValidServiceRequest(httpRequest)) {
            log.warn("Unauthorized inventory release request from {}", httpRequest.getRemoteAddr());
            return ResponseEntity.status(403).build();
        }
        
        try {
            inventoryService.releaseReservedInventory(productId, request.getQuantity(), request.getUserId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error releasing inventory for product {}: {}", productId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/{productId}/inventory/confirm")
    public ResponseEntity<Void> confirmInventoryUsage(@PathVariable Long productId,
                                                    @RequestBody ReservationRequest request,
                                                    HttpServletRequest httpRequest) {
        if (!isValidServiceRequest(httpRequest)) {
            log.warn("Unauthorized inventory confirm request from {}", httpRequest.getRemoteAddr());
            return ResponseEntity.status(403).build();
        }
        
        try {
            inventoryService.confirmInventoryUsage(productId, request.getQuantity(), request.getUserId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error confirming inventory usage for product {}: {}", productId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Validate multiple cart items (internal service endpoint)
    @PostMapping("/validate-cart")
    public ResponseEntity<java.util.Map<String, Object>> validateCart(@RequestBody java.util.Map<String, Object> body,
                                                                      HttpServletRequest httpRequest) {
        if (!isValidServiceRequest(httpRequest)) {
            log.warn("Unauthorized cart validation request from {}", httpRequest.getRemoteAddr());
            return ResponseEntity.status(403).build();
        }

        try {
            java.util.List<java.util.Map<String, Object>> items = (java.util.List<java.util.Map<String, Object>>) body.get("items");
            if (items == null) {
                return ResponseEntity.badRequest().body(java.util.Map.of("valid", false, "errors", java.util.List.of("Missing items")));
            }

            java.util.List<com.gitanjsheth.productservice.services.InventoryService.CartItemValidation> validations = new java.util.ArrayList<>();
            for (java.util.Map<String, Object> item : items) {
                Long pId = ((Number) item.get("productId")).longValue();
                Integer qty = ((Number) item.get("quantity")).intValue();
                validations.add(new com.gitanjsheth.productservice.services.InventoryService.CartItemValidation(pId, qty));
            }

            boolean valid = inventoryService.validateCartItems(validations);
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("valid", valid);
            if (!valid) {
                result.put("errors", java.util.List.of("One or more items are unavailable"));
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error validating cart items: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Validate that request is coming from authorized internal services
     */
    private boolean isValidServiceRequest(HttpServletRequest request) {
        // Method 1: Service Token Authentication
        String serviceToken = request.getHeader("X-Service-Token");
        if (serviceToken != null) {
            return isValidServiceToken(serviceToken);
        }
        
        // Method 2: Service Name Header (for development)
        String serviceName = request.getHeader("X-Service-Name");
        if (serviceName != null) {
            return isAuthorizedService(serviceName);
        }
        
        // Method 3: IP-based validation (for same-host deployment)
        String clientIP = getClientIP(request);
        return isLocalhost(clientIP);
    }
    
    private boolean isValidServiceToken(String token) {
        // Prefer application property; fallback to env; finally to default constant for dev
        if (configuredServiceToken != null && !configuredServiceToken.isEmpty()) {
            return configuredServiceToken.equals(token);
        }
        String envToken = System.getenv("INTERNAL_SERVICE_TOKEN");
        if (envToken != null && !envToken.isEmpty()) {
            return envToken.equals(token);
        }
        return "internal-service-secret-2024".equals(token);
    }
    
    private boolean isAuthorizedService(String serviceName) {
        // Allow known internal services
        return serviceName.equals("CartService") || 
               serviceName.equals("OrderManagementService") ||
               serviceName.equals("PaymentService");
    }
    
    private boolean isLocalhost(String ip) {
        // Allow localhost/local network calls (for development/same-host deployment)
        return "127.0.0.1".equals(ip) || 
               "0:0:0:0:0:0:0:1".equals(ip) || 
               "::1".equals(ip) ||
               ip.startsWith("192.168.") ||
               ip.startsWith("10.") ||
               ip.startsWith("172.");
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
    
    // Request DTO
    public static class ReservationRequest {
        private Integer quantity;
        private Long userId;
        
        public ReservationRequest() {}
        
        public ReservationRequest(Integer quantity, Long userId) {
            this.quantity = quantity;
            this.userId = userId;
        }
        
        // Getters and setters
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }
}
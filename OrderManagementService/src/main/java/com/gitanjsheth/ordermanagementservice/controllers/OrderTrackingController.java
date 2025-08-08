package com.gitanjsheth.ordermanagementservice.controllers;

import com.gitanjsheth.ordermanagementservice.dtos.OrderTrackingDto;
import com.gitanjsheth.ordermanagementservice.security.SecurityUtils;
import com.gitanjsheth.ordermanagementservice.services.OrderTrackingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
@Slf4j
public class OrderTrackingController {
    
    private final OrderTrackingService orderTrackingService;
    private final SecurityUtils securityUtils;
    
    @Value("${app.service.token:}")
    private String configuredServiceToken;
    
    /**
     * Track order by order number (public endpoint with validation)
     */
    @GetMapping("/order/{orderNumber}")
    public ResponseEntity<OrderTrackingDto> trackOrderByNumber(
            @PathVariable String orderNumber,
            @RequestParam(required = false) String phone,
            HttpServletRequest request) {
        
        // For internal service calls
        if (isValidServiceRequest(request)) {
            OrderTrackingDto tracking = orderTrackingService.getOrderTracking(orderNumber);
            return ResponseEntity.ok(tracking);
        }
        
        // For authenticated users
        if (securityUtils.isAuthenticated()) {
            Long userId = securityUtils.getCurrentUserId();
            OrderTrackingDto tracking = orderTrackingService.getOrderTrackingForUser(orderNumber, userId);
            return ResponseEntity.ok(tracking);
        }
        
        // For guest users - require phone number validation (simplified)
        if (phone != null && !phone.trim().isEmpty()) {
            // In a real implementation, you would validate the phone number against the order
            // For now, we'll allow tracking if phone is provided
            OrderTrackingDto tracking = orderTrackingService.getOrderTracking(orderNumber);
            return ResponseEntity.ok(tracking);
        }
        
        return ResponseEntity.status(403).build();
    }
    
    /**
     * Track order for authenticated user
     */
    @GetMapping("/my-order/{orderNumber}")
    public ResponseEntity<OrderTrackingDto> trackMyOrder(@PathVariable String orderNumber) {
        
        if (!securityUtils.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        Long userId = securityUtils.getCurrentUserId();
        OrderTrackingDto tracking = orderTrackingService.getOrderTrackingForUser(orderNumber, userId);
        return ResponseEntity.ok(tracking);
    }
    
    /**
     * Update tracking information (internal service endpoint)
     */
    @PostMapping("/update/{orderNumber}")
    public ResponseEntity<String> updateTracking(
            @PathVariable String orderNumber,
            @RequestBody UpdateTrackingRequest request,
            HttpServletRequest httpRequest) {
        
        if (!isValidServiceRequest(httpRequest)) {
            return ResponseEntity.status(403).build();
        }
        
        orderTrackingService.updateOrderTracking(orderNumber, 
            request.getTrackingNumber(), request.getLocation());
        
        return ResponseEntity.ok("Tracking updated successfully");
    }
    
    // ============================================================================
    // HELPER METHODS
    // ============================================================================
    
    private boolean isValidServiceRequest(HttpServletRequest request) {
        String serviceToken = request.getHeader("X-Service-Token");
        if (serviceToken != null && !serviceToken.isEmpty()) {
            return isValidServiceToken(serviceToken);
        }
        
        String serviceName = request.getHeader("X-Service-Name");
        if (serviceName != null) {
            return isAuthorizedService(serviceName);
        }
        
        String clientIP = getClientIP(request);
        return isLocalhost(clientIP);
    }
    
    private boolean isValidServiceToken(String token) {
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
        List<String> authorizedServices = List.of(
            "CartService", 
            "PaymentService", 
            "UserAuthService",
            "NotificationService",
            "AdminService"
        );
        return authorizedServices.contains(serviceName);
    }
    
    private boolean isLocalhost(String ip) {
        return "127.0.0.1".equals(ip) || "::1".equals(ip) || "localhost".equals(ip);
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
    
    // ============================================================================
    // DTOs
    // ============================================================================
    
    public static class UpdateTrackingRequest {
        private String trackingNumber;
        private String location;
        
        public String getTrackingNumber() { return trackingNumber; }
        public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
        
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }
}
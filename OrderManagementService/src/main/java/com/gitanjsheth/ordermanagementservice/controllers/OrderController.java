package com.gitanjsheth.ordermanagementservice.controllers;

import com.gitanjsheth.ordermanagementservice.dtos.CreateOrderDto;
import com.gitanjsheth.ordermanagementservice.dtos.OrderDto;
import com.gitanjsheth.ordermanagementservice.models.OrderStatus;
import com.gitanjsheth.ordermanagementservice.services.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    
    private final OrderService orderService;
    
    @Value("${app.service.token}")
    private String serviceToken;
    
    // ============================================================================
    // INTERNAL SERVICE ENDPOINTS (Service-to-Service Communication)
    // ============================================================================
    
    /**
     * Create order from cart (called by frontend via CartService)
     */
    @PostMapping
    public ResponseEntity<OrderDto> createOrder(
            @Valid @RequestBody CreateOrderDto createOrderDto,
            @RequestHeader("X-User-Id") Long userId,
            HttpServletRequest request) {
        
        if (!isValidServiceRequest(request)) {
            return ResponseEntity.status(403).build();
        }
        
        OrderDto order = orderService.createOrderFromCart(createOrderDto, userId);
        return ResponseEntity.ok(order);
    }
    
    /**
     * Get user's orders
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<OrderDto>> getUserOrders(
            @PathVariable Long userId,
            Pageable pageable,
            HttpServletRequest request) {
        
        if (!isValidServiceRequest(request)) {
            return ResponseEntity.status(403).build();
        }
        
        Page<OrderDto> orders = orderService.getUserOrders(userId, pageable);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * Get user's orders by status
     */
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<Page<OrderDto>> getUserOrdersByStatus(
            @PathVariable Long userId,
            @PathVariable OrderStatus status,
            Pageable pageable,
            HttpServletRequest request) {
        
        if (!isValidServiceRequest(request)) {
            return ResponseEntity.status(403).build();
        }
        
        Page<OrderDto> orders = orderService.getUserOrdersByStatus(userId, status, pageable);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * Get order by ID
     */
    @GetMapping("/{orderId}/user/{userId}")
    public ResponseEntity<OrderDto> getOrderById(
            @PathVariable Long orderId,
            @PathVariable Long userId,
            HttpServletRequest request) {
        
        if (!isValidServiceRequest(request)) {
            return ResponseEntity.status(403).build();
        }
        
        OrderDto order = orderService.getOrderById(orderId, userId);
        return ResponseEntity.ok(order);
    }
    
    /**
     * Get order by order number
     */
    @GetMapping("/number/{orderNumber}/user/{userId}")
    public ResponseEntity<OrderDto> getOrderByOrderNumber(
            @PathVariable String orderNumber,
            @PathVariable Long userId,
            HttpServletRequest request) {
        
        if (!isValidServiceRequest(request)) {
            return ResponseEntity.status(403).build();
        }
        
        OrderDto order = orderService.getOrderByOrderNumber(orderNumber, userId);
        return ResponseEntity.ok(order);
    }
    
    /**
     * Cancel order
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(
            @PathVariable Long orderId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        
        if (!isValidServiceRequest(httpRequest)) {
            return ResponseEntity.status(403).build();
        }
        
        String reason = request.getOrDefault("reason", "User requested cancellation");
        OrderDto order = orderService.cancelOrder(orderId, userId, reason);
        return ResponseEntity.ok(order);
    }
    
    // ============================================================================
    // ADMIN ENDPOINTS (Internal Service Only)
    // ============================================================================
    
    /**
     * Get all orders (admin only)
     */
    @GetMapping("/admin/all")
    public ResponseEntity<Page<OrderDto>> getAllOrders(
            Pageable pageable,
            HttpServletRequest request) {
        
        if (!isValidServiceRequest(request)) {
            return ResponseEntity.status(403).build();
        }
        
        Page<OrderDto> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * Get orders by status (admin only)
     */
    @GetMapping("/admin/status/{status}")
    public ResponseEntity<List<OrderDto>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            HttpServletRequest request) {
        
        if (!isValidServiceRequest(request)) {
            return ResponseEntity.status(403).build();
        }
        
        List<OrderDto> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * Update order status (admin only)
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        
        if (!isValidServiceRequest(httpRequest)) {
            return ResponseEntity.status(403).build();
        }
        
        OrderStatus newStatus = OrderStatus.valueOf(request.get("status"));
        OrderDto order = orderService.updateOrderStatus(orderId, newStatus);
        return ResponseEntity.ok(order);
    }
    
    /**
     * Mark order as shipped
     */
    @PostMapping("/{orderId}/ship")
    public ResponseEntity<OrderDto> markOrderAsShipped(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        
        if (!isValidServiceRequest(httpRequest)) {
            return ResponseEntity.status(403).build();
        }
        
        String trackingNumber = request.get("trackingNumber");
        OrderDto order = orderService.markOrderAsShipped(orderId, trackingNumber);
        return ResponseEntity.ok(order);
    }
    
    /**
     * Mark order as delivered
     */
    @PostMapping("/{orderId}/deliver")
    public ResponseEntity<OrderDto> markOrderAsDelivered(
            @PathVariable Long orderId,
            HttpServletRequest request) {
        
        if (!isValidServiceRequest(request)) {
            return ResponseEntity.status(403).build();
        }
        
        OrderDto order = orderService.markOrderAsDelivered(orderId);
        return ResponseEntity.ok(order);
    }
    
    // ============================================================================
    // PAYMENT SERVICE INTEGRATION ENDPOINTS
    // ============================================================================
    
    /**
     * Handle payment completion (called by PaymentService)
     */
    @PostMapping("/{orderId}/payment/completed")
    public ResponseEntity<String> handlePaymentCompleted(
            @PathVariable String orderId,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        
        if (!isValidServiceRequest(httpRequest)) {
            return ResponseEntity.status(403).build();
        }
        
        String paymentId = request.get("paymentId");
        orderService.handlePaymentCompleted(orderId, paymentId);
        return ResponseEntity.ok("Payment completion processed");
    }
    
    /**
     * Handle payment failure (called by PaymentService)
     */
    @PostMapping("/{orderId}/payment/failed")
    public ResponseEntity<String> handlePaymentFailed(
            @PathVariable String orderId,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        
        if (!isValidServiceRequest(httpRequest)) {
            return ResponseEntity.status(403).build();
        }
        
        String reason = request.get("reason");
        orderService.handlePaymentFailed(orderId, reason);
        return ResponseEntity.ok("Payment failure processed");
    }
    
    // ============================================================================
    // INVENTORY MANAGEMENT ENDPOINTS (Internal Only)
    // ============================================================================
    
    /**
     * Reserve inventory for order
     */
    @PostMapping("/{orderId}/inventory/reserve")
    public ResponseEntity<Map<String, Boolean>> reserveInventory(
            @PathVariable Long orderId,
            HttpServletRequest request) {
        
        if (!isValidServiceRequest(request)) {
            return ResponseEntity.status(403).build();
        }
        
        boolean success = orderService.reserveInventoryForOrder(orderId);
        return ResponseEntity.ok(Map.of("success", success));
    }
    
    /**
     * Release inventory reservation
     */
    @PostMapping("/{orderId}/inventory/release")
    public ResponseEntity<String> releaseInventoryReservation(
            @PathVariable Long orderId,
            HttpServletRequest request) {
        
        if (!isValidServiceRequest(request)) {
            return ResponseEntity.status(403).build();
        }
        
        orderService.releaseInventoryReservation(orderId);
        return ResponseEntity.ok("Inventory reservation released");
    }
    
    /**
     * Confirm inventory usage
     */
    @PostMapping("/{orderId}/inventory/confirm")
    public ResponseEntity<String> confirmInventoryUsage(
            @PathVariable Long orderId,
            HttpServletRequest request) {
        
        if (!isValidServiceRequest(request)) {
            return ResponseEntity.status(403).build();
        }
        
        orderService.confirmInventoryUsage(orderId);
        return ResponseEntity.ok("Inventory usage confirmed");
    }
    
    // ============================================================================
    // HELPER METHODS
    // ============================================================================
    
    private boolean isValidServiceRequest(HttpServletRequest request) {
        String serviceToken = request.getHeader("X-Service-Token");
        if (serviceToken != null) {
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
        return serviceToken.equals(token);
    }
    
    private boolean isAuthorizedService(String serviceName) {
        List<String> authorizedServices = List.of(
            "CartService", 
            "PaymentService", 
            "UserAuthService",
            "NotificationService"
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
}
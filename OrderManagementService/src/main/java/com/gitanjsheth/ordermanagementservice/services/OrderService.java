package com.gitanjsheth.ordermanagementservice.services;

import com.gitanjsheth.ordermanagementservice.dtos.CreateOrderDto;
import com.gitanjsheth.ordermanagementservice.dtos.OrderDto;
import com.gitanjsheth.ordermanagementservice.models.Order;
import com.gitanjsheth.ordermanagementservice.models.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    
    // ============================================================================
    // ORDER LIFECYCLE MANAGEMENT
    // ============================================================================
    
    /**
     * Create order from cart and reserve inventory
     */
    OrderDto createOrderFromCart(CreateOrderDto createOrderDto, Long userId);
    
    /**
     * Get order by ID (with user validation)
     */
    OrderDto getOrderById(Long orderId, Long userId);
    
    /**
     * Get order by order number (with user validation)
     */
    OrderDto getOrderByOrderNumber(String orderNumber, Long userId);
    
    /**
     * Get user's order history
     */
    Page<OrderDto> getUserOrders(Long userId, Pageable pageable);
    
    /**
     * Get user's orders by status
     */
    Page<OrderDto> getUserOrdersByStatus(Long userId, OrderStatus status, Pageable pageable);
    
    // ============================================================================
    // ORDER STATE MANAGEMENT
    // ============================================================================
    
    /**
     * Cancel order and release inventory
     */
    OrderDto cancelOrder(Long orderId, Long userId, String reason);
    
    /**
     * Update order status (admin only)
     */
    OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus);
    
    /**
     * Mark order as shipped
     */
    OrderDto markOrderAsShipped(Long orderId, String trackingNumber);
    
    /**
     * Mark order as delivered
     */
    OrderDto markOrderAsDelivered(Long orderId);
    
    // ============================================================================
    // INVENTORY MANAGEMENT (moved from CartService)
    // ============================================================================
    
    /**
     * Reserve inventory for order (24-hour hold)
     */
    boolean reserveInventoryForOrder(Long orderId);
    
    /**
     * Release inventory reservation (on order cancellation)
     */
    void releaseInventoryReservation(Long orderId);
    
    /**
     * Confirm inventory usage (on payment success)
     */
    void confirmInventoryUsage(Long orderId);
    
    // ============================================================================
    // PAYMENT INTEGRATION
    // ============================================================================
    
    /**
     * Process payment completion (called by PaymentService)
     */
    void handlePaymentCompleted(String orderId, String paymentId);
    
    /**
     * Process payment failure (called by PaymentService)
     */
    void handlePaymentFailed(String orderId, String reason);
    
    // ============================================================================
    // ADMIN OPERATIONS
    // ============================================================================
    
    /**
     * Get all orders (admin only)
     */
    Page<OrderDto> getAllOrders(Pageable pageable);
    
    /**
     * Get orders by status (admin only)
     */
    List<OrderDto> getOrdersByStatus(OrderStatus status);
    
    /**
     * Cleanup expired inventory reservations
     */
    void cleanupExpiredInventoryReservations();
    
    /**
     * Auto-cancel old unpaid orders
     */
    void autoCancelOldOrders();
}
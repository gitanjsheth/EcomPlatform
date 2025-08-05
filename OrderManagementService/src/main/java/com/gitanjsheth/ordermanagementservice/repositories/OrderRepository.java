package com.gitanjsheth.ordermanagementservice.repositories;

import com.gitanjsheth.ordermanagementservice.models.Order;
import com.gitanjsheth.ordermanagementservice.models.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Find orders by user
    Page<Order> findByUserIdOrderByOrderDateDesc(Long userId, Pageable pageable);
    
    // Find order by order number
    Optional<Order> findByOrderNumber(String orderNumber);
    
    // Find orders by status
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByStatusIn(List<OrderStatus> statuses);
    
    // Find orders by user and status
    Page<Order> findByUserIdAndStatusOrderByOrderDateDesc(Long userId, OrderStatus status, Pageable pageable);
    
    // Find orders with expired inventory reservations
    @Query("SELECT o FROM Order o WHERE o.inventoryReserved = true AND o.inventoryReservationExpiresAt < :now")
    List<Order> findOrdersWithExpiredInventoryReservations(@Param("now") LocalDateTime now);
    
    // Find orders created in date range
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate ORDER BY o.orderDate DESC")
    List<Order> findOrdersInDateRange(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);
    
    // Find orders by payment status
    List<Order> findByPaymentStatus(com.gitanjsheth.ordermanagementservice.models.PaymentStatus paymentStatus);
    
    // Count orders by user
    long countByUserId(Long userId);
    
    // Count orders by status
    long countByStatus(OrderStatus status);
    
    // Find orders that need auto-cancellation
    @Query("SELECT o FROM Order o WHERE o.status IN ('CREATED', 'PAYMENT_PENDING') AND o.orderDate < :cutoffTime")
    List<Order> findOrdersForAutoCancellation(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Find recent orders (for admin dashboard)
    @Query("SELECT o FROM Order o ORDER BY o.orderDate DESC")
    Page<Order> findRecentOrders(Pageable pageable);
    
    // Find orders by cart ID
    Optional<Order> findByCartId(String cartId);
}
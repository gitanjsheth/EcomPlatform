package com.gitanjsheth.ordermanagementservice.services;

import com.gitanjsheth.ordermanagementservice.dtos.CreateOrderDto;
import com.gitanjsheth.ordermanagementservice.dtos.OrderDto;
import com.gitanjsheth.ordermanagementservice.events.InventoryEvent;
import com.gitanjsheth.ordermanagementservice.events.OrderEvent;
import com.gitanjsheth.ordermanagementservice.messaging.OrderEventPublisher;
import com.gitanjsheth.ordermanagementservice.models.*;
import com.gitanjsheth.ordermanagementservice.repositories.OrderRepository;
import com.gitanjsheth.ordermanagementservice.utils.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {
    
    private final OrderRepository orderRepository;
    private final CartIntegrationService cartIntegrationService;
    private final ProductIntegrationService productIntegrationService;
    private final PaymentIntegrationService paymentIntegrationService;
    private final OrderEventPublisher eventPublisher;
    private final OrderMapper orderMapper;
    
    @Value("${app.order.number-prefix:ORD}")
    private String orderNumberPrefix;
    
    @Value("${app.order.default-shipping-days:7}")
    private int defaultShippingDays;
    
    @Value("${app.order.auto-cancel-hours:24}")
    private int autoCancelHours;
    
    // ============================================================================
    // ORDER LIFECYCLE MANAGEMENT
    // ============================================================================
    
    @Override
    public OrderDto createOrderFromCart(CreateOrderDto createOrderDto, Long userId) {
        log.info("Creating order from cart {} for user {}", createOrderDto.getCartId(), userId);
        
        // 1. Get cart details
        Map<String, Object> cartData = cartIntegrationService.getCart(createOrderDto.getCartId());
        List<Map<String, Object>> cartItems = (List<Map<String, Object>>) cartData.get("items");
        
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty or not found");
        }
        
        // 2. Validate cart items availability
        Map<String, Object> validationResult = productIntegrationService.validateCartItems(cartItems);
        if (!(Boolean) validationResult.get("valid")) {
            throw new RuntimeException("Some items in cart are no longer available: " + 
                validationResult.get("errors"));
        }
        
        // 3. Create order entity
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUserId(userId);
        order.setCartId(createOrderDto.getCartId());
        order.setNotes(createOrderDto.getNotes());
        
        // Set delivery address
        DeliveryAddress deliveryAddress = orderMapper.toDeliveryAddress(createOrderDto.getDeliveryAddress());
        order.setDeliveryAddress(deliveryAddress);
        
        // Set expected delivery date
        order.setExpectedDeliveryDate(LocalDateTime.now().plusDays(defaultShippingDays));
        
        // 4. Add order items
        for (Map<String, Object> cartItem : cartItems) {
            OrderItem orderItem = new OrderItem(
                ((Number) cartItem.get("productId")).longValue(),
                (String) cartItem.get("productTitle"),
                (String) cartItem.get("productImageUrl"),
                BigDecimal.valueOf(((Number) cartItem.get("unitPrice")).doubleValue()),
                ((Number) cartItem.get("quantity")).intValue()
            );
            order.addItem(orderItem);
        }
        
        // 5. Save order
        order = orderRepository.save(order);
        log.info("Created order {} with {} items", order.getOrderNumber(), order.getItems().size());
        
        // 6. Reserve inventory
        boolean inventoryReserved = reserveInventoryForOrder(order.getId());
        if (!inventoryReserved) {
            // Rollback order creation
            orderRepository.delete(order);
            throw new RuntimeException("Failed to reserve inventory for order");
        }
        
        // 7. Mark cart as checked out
        cartIntegrationService.markCartAsCheckedOut(createOrderDto.getCartId());
        
        // 8. Publish order created event
        publishOrderEvent(order, "CREATED", null, OrderStatus.CREATED.name());
        
        return orderMapper.toDto(order);
    }
    
    @Override
    public OrderDto getOrderById(Long orderId, Long userId) {
        Order order = findOrderByIdAndUser(orderId, userId);
        return orderMapper.toDto(order);
    }
    
    @Override
    public OrderDto getOrderByOrderNumber(String orderNumber, Long userId) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));
        
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied to order: " + orderNumber);
        }
        
        return orderMapper.toDto(order);
    }
    
    @Override
    public Page<OrderDto> getUserOrders(Long userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserIdOrderByOrderDateDesc(userId, pageable);
        return orders.map(orderMapper::toDto);
    }
    
    @Override
    public Page<OrderDto> getUserOrdersByStatus(Long userId, OrderStatus status, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserIdAndStatusOrderByOrderDateDesc(userId, status, pageable);
        return orders.map(orderMapper::toDto);
    }
    
    // ============================================================================
    // ORDER STATE MANAGEMENT
    // ============================================================================
    
    @Override
    public OrderDto cancelOrder(Long orderId, Long userId, String reason) {
        Order order = findOrderByIdAndUser(orderId, userId);
        
        if (!order.canBeCancelled()) {
            throw new RuntimeException("Order cannot be cancelled in current status: " + order.getStatus());
        }
        
        OrderStatus previousStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        order.setNotes(order.getNotes() + "\nCancellation reason: " + reason);
        
        // Release inventory reservation
        releaseInventoryReservation(orderId);
        
        order = orderRepository.save(order);
        
        publishOrderEvent(order, "CANCELLED", previousStatus.name(), OrderStatus.CANCELLED.name());
        
        log.info("Cancelled order {} for user {}", order.getOrderNumber(), userId);
        return orderMapper.toDto(order);
    }
    
    @Override
    public OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        OrderStatus previousStatus = order.getStatus();
        order.setStatus(newStatus);
        order = orderRepository.save(order);
        
        publishOrderEvent(order, "STATUS_UPDATED", previousStatus.name(), newStatus.name());
        
        log.info("Updated order {} status from {} to {}", order.getOrderNumber(), previousStatus, newStatus);
        return orderMapper.toDto(order);
    }
    
    @Override
    public OrderDto markOrderAsShipped(Long orderId, String trackingNumber) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        order.setStatus(OrderStatus.SHIPPED);
        order.setTrackingNumber(trackingNumber);
        order = orderRepository.save(order);
        
        publishOrderEvent(order, "SHIPPED", OrderStatus.PROCESSING.name(), OrderStatus.SHIPPED.name());
        
        log.info("Marked order {} as shipped with tracking: {}", order.getOrderNumber(), trackingNumber);
        return orderMapper.toDto(order);
    }
    
    @Override
    public OrderDto markOrderAsDelivered(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        order.setStatus(OrderStatus.DELIVERED);
        order.setActualDeliveryDate(LocalDateTime.now());
        order = orderRepository.save(order);
        
        publishOrderEvent(order, "DELIVERED", OrderStatus.SHIPPED.name(), OrderStatus.DELIVERED.name());
        
        log.info("Marked order {} as delivered", order.getOrderNumber());
        return orderMapper.toDto(order);
    }
    
    // ============================================================================
    // INVENTORY MANAGEMENT (moved from CartService)
    // ============================================================================
    
    @Override
    public boolean reserveInventoryForOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        log.info("Reserving inventory for order {}", order.getOrderNumber());
        
        // Reserve inventory for each item
        List<Boolean> reservationResults = order.getItems().stream()
            .map(item -> productIntegrationService.reserveInventory(
                item.getProductId(), item.getQuantity()))
            .collect(Collectors.toList());
        
        boolean allReserved = reservationResults.stream().allMatch(Boolean::booleanValue);
        
        List<InventoryEvent.InventoryItem> inventoryItems = order.getItems().stream()
            .map(item -> new InventoryEvent.InventoryItem(
                item.getProductId(), item.getQuantity(), "RESERVE"))
            .collect(Collectors.toList());
        
        if (allReserved) {
            order.markInventoryAsReserved();
            orderRepository.save(order);
            
            // Publish inventory event
            InventoryEvent event = new InventoryEvent(order.getId().toString(), "RESERVE", inventoryItems);
            eventPublisher.publishInventoryEvent(event);
            
            log.info("Successfully reserved inventory for order {}", order.getOrderNumber());
            return true;
        } else {
            log.warn("Failed to reserve inventory for order {}", order.getOrderNumber());
            return false;
        }
    }
    
    @Override
    public void releaseInventoryReservation(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        if (!order.getInventoryReserved()) {
            log.info("No inventory reservation to release for order {}", order.getOrderNumber());
            return;
        }
        
        log.info("Releasing inventory reservation for order {}", order.getOrderNumber());
        
        List<InventoryEvent.InventoryItem> inventoryItems = order.getItems().stream()
            .map(item -> {
                productIntegrationService.releaseInventory(item.getProductId(), item.getQuantity());
                return new InventoryEvent.InventoryItem(
                    item.getProductId(), item.getQuantity(), "RELEASE");
            }).collect(Collectors.toList());
        
        order.markInventoryAsReleased();
        orderRepository.save(order);
        
        // Publish inventory event
        InventoryEvent event = new InventoryEvent(order.getId().toString(), "RELEASE", inventoryItems);
        eventPublisher.publishInventoryEvent(event);
        
        log.info("Released inventory reservation for order {}", order.getOrderNumber());
    }
    
    @Override
    public void confirmInventoryUsage(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        log.info("Confirming inventory usage for order {}", order.getOrderNumber());
        
        List<InventoryEvent.InventoryItem> inventoryItems = order.getItems().stream()
            .map(item -> {
                productIntegrationService.confirmInventoryUsage(item.getProductId(), item.getQuantity());
                return new InventoryEvent.InventoryItem(
                    item.getProductId(), item.getQuantity(), "CONFIRM");
            }).collect(Collectors.toList());
        
        // Publish inventory event
        InventoryEvent event = new InventoryEvent(order.getId().toString(), "CONFIRM", inventoryItems);
        eventPublisher.publishInventoryEvent(event);
        
        log.info("Confirmed inventory usage for order {}", order.getOrderNumber());
    }
    
    // ============================================================================
    // PAYMENT INTEGRATION
    // ============================================================================
    
    @Override
    public void handlePaymentCompleted(String orderId, String paymentId) {
        Order order = orderRepository.findById(Long.valueOf(orderId))
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order.setPaymentId(paymentId);
        order.setStatus(OrderStatus.CONFIRMED);
        order = orderRepository.save(order);
        
        // Confirm inventory usage (final stock deduction)
        confirmInventoryUsage(order.getId());
        
        publishOrderEvent(order, "PAYMENT_COMPLETED", 
            OrderStatus.PAYMENT_PENDING.name(), OrderStatus.CONFIRMED.name());
        
        log.info("Payment completed for order {}, payment ID: {}", order.getOrderNumber(), paymentId);
    }
    
    @Override
    public void handlePaymentFailed(String orderId, String reason) {
        Order order = orderRepository.findById(Long.valueOf(orderId))
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        order.setPaymentStatus(PaymentStatus.FAILED);
        order.setStatus(OrderStatus.CANCELLED);
        order.setNotes(order.getNotes() + "\nPayment failed: " + reason);
        order = orderRepository.save(order);
        
        // Release inventory reservation
        releaseInventoryReservation(order.getId());
        
        publishOrderEvent(order, "PAYMENT_FAILED", 
            OrderStatus.PAYMENT_PENDING.name(), OrderStatus.CANCELLED.name());
        
        log.info("Payment failed for order {}, reason: {}", order.getOrderNumber(), reason);
    }
    
    // ============================================================================
    // ADMIN OPERATIONS
    // ============================================================================
    
    @Override
    public Page<OrderDto> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findRecentOrders(pageable);
        return orders.map(orderMapper::toDto);
    }
    
    @Override
    public List<OrderDto> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);
        return orders.stream().map(orderMapper::toDto).collect(Collectors.toList());
    }
    
    // ============================================================================
    // SCHEDULED CLEANUP TASKS
    // ============================================================================
    
    @Override
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredInventoryReservations() {
        log.info("Starting cleanup of expired inventory reservations");
        
        List<Order> expiredOrders = orderRepository.findOrdersWithExpiredInventoryReservations(LocalDateTime.now());
        
        for (Order order : expiredOrders) {
            try {
                releaseInventoryReservation(order.getId());
                log.info("Released expired inventory reservation for order {}", order.getOrderNumber());
            } catch (Exception e) {
                log.error("Failed to release expired inventory for order {}: {}", 
                    order.getOrderNumber(), e.getMessage());
            }
        }
        
        log.info("Completed cleanup of {} expired inventory reservations", expiredOrders.size());
    }
    
    @Override
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void autoCancelOldOrders() {
        log.info("Starting auto-cancellation of old unpaid orders");
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(autoCancelHours);
        List<Order> oldOrders = orderRepository.findOrdersForAutoCancellation(cutoffTime);
        
        for (Order order : oldOrders) {
            try {
                order.setStatus(OrderStatus.CANCELLED);
                order.setNotes(order.getNotes() + "\nAuto-cancelled due to timeout");
                releaseInventoryReservation(order.getId());
                orderRepository.save(order);
                
                publishOrderEvent(order, "AUTO_CANCELLED", 
                    OrderStatus.CREATED.name(), OrderStatus.CANCELLED.name());
                
                log.info("Auto-cancelled order {}", order.getOrderNumber());
            } catch (Exception e) {
                log.error("Failed to auto-cancel order {}: {}", order.getOrderNumber(), e.getMessage());
            }
        }
        
        log.info("Completed auto-cancellation of {} old orders", oldOrders.size());
    }
    
    // ============================================================================
    // HELPER METHODS
    // ============================================================================
    
    private Order findOrderByIdAndUser(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied to order: " + orderId);
        }
        
        return order;
    }
    
    private String generateOrderNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        return orderNumberPrefix + "-" + timestamp.substring(timestamp.length() - 8);
    }
    
    private void publishOrderEvent(Order order, String eventType, String previousStatus, String currentStatus) {
        OrderEvent event = new OrderEvent(
            order.getId().toString(),
            order.getOrderNumber(),
            order.getUserId(),
            eventType,
            previousStatus,
            currentStatus
        );
        eventPublisher.publishOrderEvent(event);
    }
}
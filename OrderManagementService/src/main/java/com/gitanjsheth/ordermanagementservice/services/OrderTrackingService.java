package com.gitanjsheth.ordermanagementservice.services;

import com.gitanjsheth.ordermanagementservice.dtos.OrderTrackingDto;
import com.gitanjsheth.ordermanagementservice.models.Order;
import com.gitanjsheth.ordermanagementservice.models.OrderStatus;
import com.gitanjsheth.ordermanagementservice.repositories.OrderRepository;
import com.gitanjsheth.ordermanagementservice.utils.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderTrackingService {
    
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    
    /**
     * Get order tracking information by order number
     */
    public OrderTrackingDto getOrderTracking(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));
        
        return createOrderTrackingDto(order);
    }
    
    /**
     * Get order tracking information by order number and user ID (for user validation)
     */
    public OrderTrackingDto getOrderTrackingForUser(String orderNumber, Long userId) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));
        
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied to order: " + orderNumber);
        }
        
        return createOrderTrackingDto(order);
    }
    
    /**
     * Update tracking information (called when order status changes)
     */
    public void updateOrderTracking(String orderNumber, String trackingNumber, String location) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));
        
        order.setTrackingNumber(trackingNumber);
        orderRepository.save(order);
        
        log.info("Updated tracking for order {} with tracking number: {}", orderNumber, trackingNumber);
    }
    
    private OrderTrackingDto createOrderTrackingDto(Order order) {
        OrderTrackingDto trackingDto = new OrderTrackingDto();
        trackingDto.setOrderNumber(order.getOrderNumber());
        trackingDto.setStatus(order.getStatus());
        trackingDto.setTrackingNumber(order.getTrackingNumber());
        trackingDto.setOrderDate(order.getOrderDate());
        trackingDto.setExpectedDeliveryDate(order.getExpectedDeliveryDate());
        trackingDto.setActualDeliveryDate(order.getActualDeliveryDate());
        
        if (order.getDeliveryAddress() != null) {
            trackingDto.setDeliveryAddress(orderMapper.toDto(order.getDeliveryAddress()));
        }
        
        // Generate tracking events based on order status
        trackingDto.setTrackingEvents(generateTrackingEvents(order));
        
        // Set current location and estimated delivery (mock implementation)
        trackingDto.setCurrentLocation(getCurrentLocation(order));
        trackingDto.setEstimatedDeliveryTime(getEstimatedDeliveryTime(order));
        
        return trackingDto;
    }
    
    private List<OrderTrackingDto.OrderTrackingEvent> generateTrackingEvents(Order order) {
        List<OrderTrackingDto.OrderTrackingEvent> events = new ArrayList<>();
        
        // Order created event
        events.add(new OrderTrackingDto.OrderTrackingEvent(
            "CREATED", 
            "Order has been placed and is being processed", 
            "Processing Center",
            order.getCreatedAt()
        ));
        
        // Add events based on current status
        if (order.getStatus() == OrderStatus.CONFIRMED || 
            order.getStatus() == OrderStatus.PROCESSING ||
            order.getStatus() == OrderStatus.SHIPPED ||
            order.getStatus() == OrderStatus.DELIVERED) {
            
            events.add(new OrderTrackingDto.OrderTrackingEvent(
                "CONFIRMED", 
                "Payment confirmed and order is being prepared", 
                "Processing Center",
                order.getCreatedAt().plusHours(1)
            ));
        }
        
        if (order.getStatus() == OrderStatus.PROCESSING ||
            order.getStatus() == OrderStatus.SHIPPED ||
            order.getStatus() == OrderStatus.DELIVERED) {
            
            events.add(new OrderTrackingDto.OrderTrackingEvent(
                "PROCESSING", 
                "Order is being prepared for shipment", 
                "Fulfillment Center",
                order.getCreatedAt().plusHours(24)
            ));
        }
        
        if (order.getStatus() == OrderStatus.SHIPPED ||
            order.getStatus() == OrderStatus.DELIVERED) {
            
            events.add(new OrderTrackingDto.OrderTrackingEvent(
                "SHIPPED", 
                "Order has been shipped and is in transit", 
                "Distribution Center",
                order.getCreatedAt().plusDays(2)
            ));
        }
        
        if (order.getStatus() == OrderStatus.DELIVERED) {
            events.add(new OrderTrackingDto.OrderTrackingEvent(
                "DELIVERED", 
                "Order has been successfully delivered", 
                order.getDeliveryAddress().getCity(),
                order.getActualDeliveryDate()
            ));
        }
        
        return events;
    }
    
    private String getCurrentLocation(Order order) {
        switch (order.getStatus()) {
            case CREATED:
            case CONFIRMED:
                return "Processing Center";
            case PROCESSING:
                return "Fulfillment Center";
            case SHIPPED:
                return "In Transit - " + order.getDeliveryAddress().getCity();
            case DELIVERED:
                return "Delivered - " + order.getDeliveryAddress().getFormattedAddress();
            case CANCELLED:
                return "Order Cancelled";
            default:
                return "Unknown";
        }
    }
    
    private String getEstimatedDeliveryTime(Order order) {
        if (order.getStatus() == OrderStatus.DELIVERED) {
            return "Delivered on " + order.getActualDeliveryDate().toLocalDate();
        }
        
        if (order.getExpectedDeliveryDate() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (order.getExpectedDeliveryDate().isAfter(now)) {
                long daysRemaining = java.time.Duration.between(now, order.getExpectedDeliveryDate()).toDays();
                if (daysRemaining == 0) {
                    return "Expected today";
                } else if (daysRemaining == 1) {
                    return "Expected tomorrow";
                } else {
                    return "Expected in " + daysRemaining + " days";
                }
            }
        }
        
        return "Delivery date not available";
    }
}
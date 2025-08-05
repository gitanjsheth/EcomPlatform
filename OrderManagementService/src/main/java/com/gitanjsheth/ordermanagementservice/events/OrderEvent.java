package com.gitanjsheth.ordermanagementservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private String orderId;
    private String orderNumber;
    private Long userId;
    private String eventType; // CREATED, CONFIRMED, CANCELLED, SHIPPED, DELIVERED
    private String previousStatus;
    private String currentStatus;
    private LocalDateTime timestamp;
    private Object data; // Additional event-specific data
    
    public OrderEvent(String orderId, String orderNumber, Long userId, String eventType, 
                     String previousStatus, String currentStatus) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.eventType = eventType;
        this.previousStatus = previousStatus;
        this.currentStatus = currentStatus;
        this.timestamp = LocalDateTime.now();
    }
}
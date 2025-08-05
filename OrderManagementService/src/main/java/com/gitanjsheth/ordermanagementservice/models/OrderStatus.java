package com.gitanjsheth.ordermanagementservice.models;

public enum OrderStatus {
    CREATED,          // Order created, awaiting payment initiation
    PAYMENT_PENDING,  // Payment gateway processing
    CONFIRMED,        // Payment successful, inventory reserved
    PROCESSING,       // Order being prepared
    SHIPPED,          // Order dispatched
    DELIVERED,        // Order delivered
    CANCELLED,        // Order cancelled
    REFUNDED          // Order refunded
}
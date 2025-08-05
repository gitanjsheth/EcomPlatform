package com.gitanjsheth.ordermanagementservice.models;

public enum PaymentStatus {
    PENDING,          // Payment not yet initiated
    PROCESSING,       // Payment being processed
    COMPLETED,        // Payment successful
    FAILED,           // Payment failed
    REFUNDED,         // Payment refunded
    CANCELLED         // Payment cancelled
}
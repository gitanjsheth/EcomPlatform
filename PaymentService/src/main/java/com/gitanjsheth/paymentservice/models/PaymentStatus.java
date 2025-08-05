package com.gitanjsheth.paymentservice.models;

public enum PaymentStatus {
    PENDING,          // Payment initiated but not processed
    PROCESSING,       // Payment being processed by gateway
    COMPLETED,        // Payment successful
    FAILED,           // Payment failed
    CANCELLED,        // Payment cancelled
    REFUNDED,         // Payment refunded
    PARTIALLY_REFUNDED // Payment partially refunded
}
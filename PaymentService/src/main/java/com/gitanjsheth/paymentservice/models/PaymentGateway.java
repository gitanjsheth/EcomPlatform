package com.gitanjsheth.paymentservice.models;

public enum PaymentGateway {
    STRIPE("Stripe"),
    PAYPAL("PayPal"),
    RAZORPAY("Razorpay"),
    MOCK("Mock Gateway"); // For testing
    
    private final String displayName;
    
    PaymentGateway(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
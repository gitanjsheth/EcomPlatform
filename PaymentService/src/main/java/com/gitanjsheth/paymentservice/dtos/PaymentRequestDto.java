package com.gitanjsheth.paymentservice.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentRequestDto {
    
    @NotBlank(message = "Order ID is required")
    private String orderId;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    private String currency = "USD";
    
    // Payment method information
    private Long paymentMethodId; // Existing saved payment method
    
    // For new card payments
    private String cardNumber;
    private String expiryMonth;
    private String expiryYear;
    private String cvv;
    private String cardholderName;
    
    // Billing address
    private String billingAddressLine1;
    private String billingAddressLine2;
    private String billingCity;
    private String billingState;
    private String billingZipCode;
    private String billingCountry;
    
    // Additional options
    private Boolean savePaymentMethod = false;
    private Boolean setAsDefault = false;
    private String description;
    private String metadata; // JSON string for additional data
    
    // Client information
    private String clientIP;
    private String userAgent;
    
    public boolean hasExistingPaymentMethod() {
        return paymentMethodId != null;
    }
    
    public boolean hasNewCardDetails() {
        return cardNumber != null && !cardNumber.trim().isEmpty();
    }
}
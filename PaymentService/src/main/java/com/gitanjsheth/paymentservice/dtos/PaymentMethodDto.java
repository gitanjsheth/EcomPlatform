package com.gitanjsheth.paymentservice.dtos;

import com.gitanjsheth.paymentservice.models.PaymentGateway;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentMethodDto {
    
    private Long id;
    private Long userId;
    private String type;
    private String cardLastFour;
    private String cardBrand;
    private Integer expiryMonth;
    private Integer expiryYear;
    private String cardholderName;
    private PaymentGateway gateway;
    private Boolean isDefault;
    private Boolean isActive;
    private String billingAddressLine1;
    private String billingAddressLine2;
    private String billingCity;
    private String billingState;
    private String billingZipCode;
    private String billingCountry;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed fields
    private String displayName;
    private String maskedCardNumber;
    private Boolean isExpired;
    
    public String getDisplayName() {
        if (cardBrand != null && cardLastFour != null) {
            return cardBrand + " ending in " + cardLastFour;
        } else if (type != null) {
            return type.replace("_", " ");
        }
        return "Payment Method";
    }
    
    public String getMaskedCardNumber() {
        if (cardLastFour != null) {
            return "**** **** **** " + cardLastFour;
        }
        return null;
    }
    
    public Boolean getIsExpired() {
        if (expiryMonth == null || expiryYear == null) {
            return false;
        }
        
        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.LocalDate expiry = java.time.LocalDate.of(expiryYear, expiryMonth, 1)
            .withDayOfMonth(java.time.LocalDate.of(expiryYear, expiryMonth, 1).lengthOfMonth());
        
        return now.isAfter(expiry);
    }
}
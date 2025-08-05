package com.gitanjsheth.paymentservice.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Getter
@Setter
@Entity
@Table(name = "payment_methods")
@Where(clause = "deleted = false")
@SQLDelete(sql = "UPDATE payment_methods SET deleted = true WHERE id = ?")
public class PaymentMethod extends BaseModel {
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "type", nullable = false)
    private String type; // CREDIT_CARD, DEBIT_CARD, PAYPAL, etc.
    
    @Column(name = "card_last_four")
    private String cardLastFour;
    
    @Column(name = "card_brand")
    private String cardBrand; // VISA, MASTERCARD, etc.
    
    @Column(name = "expiry_month")
    private Integer expiryMonth;
    
    @Column(name = "expiry_year")
    private Integer expiryYear;
    
    @Column(name = "cardholder_name")
    private String cardholderName;
    
    @Column(name = "gateway_token")
    private String gatewayToken; // Token from payment gateway
    
    @Enumerated(EnumType.STRING)
    @Column(name = "gateway", nullable = false)
    private PaymentGateway gateway;
    
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "billing_address_line1")
    private String billingAddressLine1;
    
    @Column(name = "billing_address_line2")
    private String billingAddressLine2;
    
    @Column(name = "billing_city")
    private String billingCity;
    
    @Column(name = "billing_state")
    private String billingState;
    
    @Column(name = "billing_zip_code")
    private String billingZipCode;
    
    @Column(name = "billing_country")
    private String billingCountry;
    
    // Business methods
    
    public boolean isExpired() {
        if (expiryMonth == null || expiryYear == null) {
            return false;
        }
        
        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.LocalDate expiry = java.time.LocalDate.of(expiryYear, expiryMonth, 1)
            .withDayOfMonth(java.time.LocalDate.of(expiryYear, expiryMonth, 1).lengthOfMonth());
        
        return now.isAfter(expiry);
    }
    
    public String getMaskedCardNumber() {
        if (cardLastFour != null) {
            return "**** **** **** " + cardLastFour;
        }
        return null;
    }
    
    public String getDisplayName() {
        if (cardBrand != null && cardLastFour != null) {
            return cardBrand + " ending in " + cardLastFour;
        } else if (type != null) {
            return type.replace("_", " ");
        }
        return "Payment Method";
    }
}
package com.gitanjsheth.paymentservice.dtos;

import com.gitanjsheth.paymentservice.models.PaymentGateway;
import com.gitanjsheth.paymentservice.models.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentResponseDto {
    
    private Long id;
    private String paymentId;
    private String orderId;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private PaymentGateway gateway;
    private String gatewayTransactionId;
    private PaymentMethodDto paymentMethod;
    private LocalDateTime processedAt;
    private LocalDateTime failedAt;
    private String failureReason;
    private BigDecimal refundedAmount;
    private LocalDateTime refundedAt;
    private Integer retryCount;
    private Boolean webhookReceived;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional response fields
    private String clientSecret; // For Stripe frontend integration
    private String receiptUrl;
    private Boolean requiresAction; // For 3D Secure or other authentication
    private String actionUrl; // URL for additional authentication
    
    // Helper methods
    public boolean isSuccessful() {
        return status == PaymentStatus.COMPLETED;
    }
    
    public boolean isFailed() {
        return status == PaymentStatus.FAILED;
    }
    
    public boolean requiresAdditionalAction() {
        return requiresAction != null && requiresAction;
    }
    
    public boolean canBeRefunded() {
        return status == PaymentStatus.COMPLETED && 
               (refundedAmount == null || refundedAmount.compareTo(amount) < 0);
    }
}
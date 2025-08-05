package com.gitanjsheth.paymentservice.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "payments")
@Where(clause = "deleted = false")
@SQLDelete(sql = "UPDATE payments SET deleted = true WHERE id = ?")
public class Payment extends BaseModel {
    
    @Column(name = "payment_id", unique = true, nullable = false)
    private String paymentId; // External payment ID from gateway
    
    @NotNull(message = "Order ID is required")
    @Column(name = "order_id", nullable = false)
    private String orderId;
    
    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Positive(message = "Amount must be positive")
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "currency", nullable = false)
    private String currency = "USD";
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "gateway", nullable = false)
    private PaymentGateway gateway;
    
    @Column(name = "gateway_transaction_id")
    private String gatewayTransactionId;
    
    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse; // JSON response from gateway
    
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "failed_at")
    private LocalDateTime failedAt;
    
    @Column(name = "failure_reason")
    private String failureReason;
    
    @Positive(message = "Refunded amount cannot be negative")
    @Column(name = "refunded_amount", precision = 19, scale = 2)
    private BigDecimal refundedAmount = BigDecimal.ZERO;
    
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;
    
    @Column(name = "webhook_received", nullable = false)
    private Boolean webhookReceived = false;
    
    @Column(name = "webhook_received_at")
    private LocalDateTime webhookReceivedAt;
    
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;
    
    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries = 3;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // Additional payment metadata as JSON
    
    // Business methods
    
    public boolean canBeRetried() {
        return status == PaymentStatus.FAILED && retryCount < maxRetries;
    }
    
    public boolean canBeRefunded() {
        return status == PaymentStatus.COMPLETED && 
               (refundedAmount == null || refundedAmount.compareTo(amount) < 0);
    }
    
    public BigDecimal getRefundableAmount() {
        if (!canBeRefunded()) {
            return BigDecimal.ZERO;
        }
        return amount.subtract(refundedAmount != null ? refundedAmount : BigDecimal.ZERO);
    }
    
    public void markAsCompleted(String gatewayTransactionId, String gatewayResponse) {
        this.status = PaymentStatus.COMPLETED;
        this.gatewayTransactionId = gatewayTransactionId;
        this.gatewayResponse = gatewayResponse;
        this.processedAt = LocalDateTime.now();
    }
    
    public void markAsFailed(String failureReason, String gatewayResponse) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = failureReason;
        this.gatewayResponse = gatewayResponse;
        this.failedAt = LocalDateTime.now();
        this.retryCount++;
    }
    
    public void markAsRefunded(BigDecimal refundAmount) {
        this.refundedAmount = (this.refundedAmount != null ? this.refundedAmount : BigDecimal.ZERO)
            .add(refundAmount);
        
        if (this.refundedAmount.compareTo(this.amount) >= 0) {
            this.status = PaymentStatus.REFUNDED;
        } else {
            this.status = PaymentStatus.PARTIALLY_REFUNDED;
        }
        
        this.refundedAt = LocalDateTime.now();
    }
}
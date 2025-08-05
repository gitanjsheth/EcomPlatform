package com.gitanjsheth.paymentservice.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "transaction_logs")
public class TransactionLog extends BaseModel {
    
    @Column(name = "payment_id", nullable = false)
    private String paymentId;
    
    @Column(name = "action", nullable = false)
    private String action; // INITIATE, PROCESS, SUCCESS, FAIL, REFUND, etc.
    
    @Enumerated(EnumType.STRING)
    @Column(name = "gateway", nullable = false)
    private PaymentGateway gateway;
    
    @Column(name = "request_data", columnDefinition = "TEXT")
    private String requestData;
    
    @Column(name = "response_data", columnDefinition = "TEXT")
    private String responseData;
    
    @Column(name = "response_code")
    private String responseCode;
    
    @Column(name = "response_message")
    private String responseMessage;
    
    @Column(name = "processing_time_ms")
    private Long processingTimeMs;
    
    @Column(name = "success", nullable = false)
    private Boolean success = false;
    
    @Column(name = "error_code")
    private String errorCode;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt = LocalDateTime.now();
    
    // Constructor for easy logging
    public TransactionLog(String paymentId, String action, PaymentGateway gateway) {
        this.paymentId = paymentId;
        this.action = action;
        this.gateway = gateway;
        this.loggedAt = LocalDateTime.now();
    }
    
    public TransactionLog() {}
    
    public void markSuccess(String responseCode, String responseMessage, Long processingTime) {
        this.success = true;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.processingTimeMs = processingTime;
    }
    
    public void markFailure(String errorCode, String errorMessage, Long processingTime) {
        this.success = false;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.processingTimeMs = processingTime;
    }
}
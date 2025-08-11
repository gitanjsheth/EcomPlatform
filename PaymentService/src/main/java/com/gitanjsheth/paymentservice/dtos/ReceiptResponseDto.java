package com.gitanjsheth.paymentservice.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for receipt generation responses
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptResponseDto {
    
    /**
     * Unique receipt identifier
     */
    private String receiptId;
    
    /**
     * Payment ID associated with this receipt
     */
    private String paymentId;
    
    /**
     * Order ID associated with this receipt
     */
    private String orderId;
    
    /**
     * User ID who made the payment
     */
    private Long userId;
    
    /**
     * Receipt format (HTML, TEXT, PDF)
     */
    private String format;
    
    /**
     * Receipt content URL
     */
    private String receiptUrl;
    
    /**
     * Receipt content (for immediate display)
     */
    private String receiptContent;
    
    /**
     * Whether receipt generation was successful
     */
    private Boolean success;
    
    /**
     * Error message if generation failed
     */
    private String errorMessage;
    
    /**
     * Timestamp when receipt was generated
     */
    private LocalDateTime generatedAt;
    
    /**
     * Receipt metadata (additional information)
     */
    private String metadata;
    
    /**
     * Constructor for successful receipt generation
     */
    public ReceiptResponseDto(String receiptId, String paymentId, String orderId, Long userId, 
                             String format, String receiptUrl, String receiptContent) {
        this.receiptId = receiptId;
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.userId = userId;
        this.format = format;
        this.receiptUrl = receiptUrl;
        this.receiptContent = receiptContent;
        this.success = true;
        this.generatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor for failed receipt generation
     */
    public ReceiptResponseDto(String paymentId, String errorMessage) {
        this.paymentId = paymentId;
        this.errorMessage = errorMessage;
        this.success = false;
        this.generatedAt = LocalDateTime.now();
    }
}

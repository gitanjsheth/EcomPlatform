package com.gitanjsheth.paymentservice.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for receipt generation requests
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptRequestDto {
    
    /**
     * Payment ID for which to generate receipt
     */
    private String paymentId;
    
    /**
     * Format preference for the receipt (HTML, TEXT, PDF)
     */
    private String format = "HTML";
    
    /**
     * Whether to include payment method details
     */
    private Boolean includePaymentMethod = true;
    
    /**
     * Whether to include transaction details
     */
    private Boolean includeTransactionDetails = true;
    
    /**
     * Custom message to include in receipt
     */
    private String customMessage;
    
    /**
     * Language preference for receipt (default: EN)
     */
    private String language = "EN";
}

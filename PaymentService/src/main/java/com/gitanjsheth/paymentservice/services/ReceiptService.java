package com.gitanjsheth.paymentservice.services;

import com.gitanjsheth.paymentservice.models.Payment;
import com.gitanjsheth.paymentservice.models.PaymentMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class ReceiptService {
    
    private static final Logger log = LoggerFactory.getLogger(ReceiptService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Generates a receipt for a completed payment
     * @param payment The payment to generate receipt for
     * @return Receipt URL or identifier
     */
    public String generateReceipt(Payment payment) {
        if (payment == null || payment.getStatus() == null) {
            log.warn("Cannot generate receipt for null or invalid payment");
            return null;
        }
        
        if (payment.getStatus().name().equals("COMPLETED")) {
            String receiptId = "RECEIPT_" + UUID.randomUUID().toString().substring(0, 8);
            log.info("Generated receipt {} for payment {}", receiptId, payment.getPaymentId());
            
            // In a real implementation, this would:
            // 1. Generate a PDF receipt
            // 2. Store it in cloud storage (S3, etc.)
            // 3. Return the public URL
            // 4. Store receipt metadata in database
            
            return receiptId;
        } else {
            log.warn("Cannot generate receipt for payment {} with status {}", 
                    payment.getPaymentId(), payment.getStatus());
            return null;
        }
    }
    
    /**
     * Retrieves the receipt URL for a payment
     * @param paymentId The payment ID
     * @return Receipt URL or null if not found
     */
    public String getReceiptUrl(String paymentId) {
        if (paymentId == null || paymentId.trim().isEmpty()) {
            return null;
        }
        
        // In a real implementation, this would:
        // 1. Query the database for receipt metadata
        // 2. Return the stored receipt URL
        // 3. Handle expired or invalid receipts
        
        // For now, return a simulated receipt URL
        return "/api/payments/" + paymentId + "/receipt";
    }
    
    /**
     * Generates receipt content as HTML (for display or PDF generation)
     * @param payment The payment details
     * @return HTML content of the receipt
     */
    public String generateReceiptHtml(Payment payment) {
        if (payment == null) {
            return "<html><body><p>Invalid payment</p></body></html>";
        }
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<title>Payment Receipt</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 40px; }");
        html.append(".receipt { border: 1px solid #ddd; padding: 20px; max-width: 600px; }");
        html.append(".header { text-align: center; border-bottom: 2px solid #333; padding-bottom: 20px; }");
        html.append(".details { margin: 20px 0; }");
        html.append(".row { display: flex; justify-content: space-between; margin: 10px 0; }");
        html.append(".label { font-weight: bold; }");
        html.append(".amount { font-size: 24px; color: #28a745; font-weight: bold; }");
        html.append(".footer { text-align: center; margin-top: 30px; color: #666; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        html.append("<div class='receipt'>");
        html.append("<div class='header'>");
        html.append("<h1>Payment Receipt</h1>");
        html.append("<p>Thank you for your payment!</p>");
        html.append("</div>");
        
        html.append("<div class='details'>");
        html.append("<div class='row'>");
        html.append("<span class='label'>Receipt ID:</span>");
        html.append("<span>").append(generateReceipt(payment)).append("</span>");
        html.append("</div>");
        
        html.append("<div class='row'>");
        html.append("<span class='label'>Payment ID:</span>");
        html.append("<span>").append(payment.getPaymentId()).append("</span>");
        html.append("</div>");
        
        html.append("<div class='row'>");
        html.append("<span class='label'>Order ID:</span>");
        html.append("<span>").append(payment.getOrderId()).append("</span>");
        html.append("</div>");
        
        html.append("<div class='row'>");
        html.append("<span class='label'>Date:</span>");
        html.append("<span>").append(payment.getProcessedAt() != null ? 
                payment.getProcessedAt().format(DATE_FORMATTER) : "N/A").append("</span>");
        html.append("</div>");
        
        html.append("<div class='row'>");
        html.append("<span class='label'>Amount:</span>");
        html.append("<span class='amount'>").append(payment.getCurrency())
                .append(" ").append(payment.getAmount()).append("</span>");
        html.append("</div>");
        
        html.append("<div class='row'>");
        html.append("<span class='label'>Status:</span>");
        html.append("<span>").append(payment.getStatus()).append("</span>");
        html.append("</div>");
        
        if (payment.getGatewayTransactionId() != null) {
            html.append("<div class='row'>");
            html.append("<span class='label'>Transaction ID:</span>");
            html.append("<span>").append(payment.getGatewayTransactionId()).append("</span>");
            html.append("</div>");
        }
        
        if (payment.getPaymentMethod() != null) {
            PaymentMethod method = payment.getPaymentMethod();
            html.append("<div class='row'>");
            html.append("<span class='label'>Payment Method:</span>");
            html.append("<span>").append(method.getType())
                    .append(" **** ").append(method.getCardLastFour()).append("</span>");
            html.append("</div>");
        }
        
        html.append("</div>");
        
        html.append("<div class='footer'>");
        html.append("<p>This is an official receipt for your records.</p>");
        html.append("<p>Generated on: ").append(LocalDateTime.now().format(DATE_FORMATTER)).append("</p>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }
    
    /**
     * Generates receipt content as plain text
     * @param payment The payment details
     * @return Plain text content of the receipt
     */
    public String generateReceiptText(Payment payment) {
        if (payment == null) {
            return "Invalid payment";
        }
        
        StringBuilder text = new StringBuilder();
        text.append("PAYMENT RECEIPT\n");
        text.append("================\n\n");
        text.append("Receipt ID: ").append(generateReceipt(payment)).append("\n");
        text.append("Payment ID: ").append(payment.getPaymentId()).append("\n");
        text.append("Order ID: ").append(payment.getOrderId()).append("\n");
        text.append("Date: ").append(payment.getProcessedAt() != null ? 
                payment.getProcessedAt().format(DATE_FORMATTER) : "N/A").append("\n");
        text.append("Amount: ").append(payment.getCurrency())
                .append(" ").append(payment.getAmount()).append("\n");
        text.append("Status: ").append(payment.getStatus()).append("\n");
        
        if (payment.getGatewayTransactionId() != null) {
            text.append("Transaction ID: ").append(payment.getGatewayTransactionId()).append("\n");
        }
        
        if (payment.getPaymentMethod() != null) {
            PaymentMethod method = payment.getPaymentMethod();
            text.append("Payment Method: ").append(method.getType())
                    .append(" **** ").append(method.getCardLastFour()).append("\n");
        }
        
        text.append("\nThank you for your payment!\n");
        text.append("Generated on: ").append(LocalDateTime.now().format(DATE_FORMATTER)).append("\n");
        
        return text.toString();
    }
    
    /**
     * Validates if a receipt can be generated for a payment
     * @param payment The payment to validate
     * @return true if receipt can be generated, false otherwise
     */
    public boolean canGenerateReceipt(Payment payment) {
        return payment != null && 
               payment.getStatus() != null && 
               payment.getStatus().name().equals("COMPLETED") &&
               payment.getProcessedAt() != null;
    }
    
    /**
     * Checks if a receipt exists for a payment
     * @param paymentId The payment ID
     * @return true if receipt exists, false otherwise
     */
    public boolean receiptExists(String paymentId) {
        if (paymentId == null || paymentId.trim().isEmpty()) {
            return false;
        }
        
        // In a real implementation, this would check the database
        // For now, return true for any valid payment ID
        return paymentId.startsWith("PAY_");
    }
}
